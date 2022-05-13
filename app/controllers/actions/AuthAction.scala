/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{PersonalDetailsValidationConnector, SessionDataCacheConnector}
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.routes
import identifiers.register.{AreYouInUKId, RegisterAsBusinessId}
import identifiers.{AdministratorOrPractitionerId, JourneyId, TypedIdentifier, ValidationId}
import models.AdministratorOrPractitioner.Practitioner
import models.FeatureToggleName.FromIvToPdv
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.libs.json.Reads
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FullAuthentication @Inject()(override val authConnector: AuthConnector,
                                   config: FrontendAppConfig,
                                   userAnswersCacheConnector: UserAnswersCacheConnector,
                                   personalDetailsValidationConnector: PersonalDetailsValidationConnector,
                                   sessionDataCacheConnector: SessionDataCacheConnector,
                                   val parser: BodyParsers.Default,
                                   featureToggleConnector: FeatureToggleConnector)
                                  (implicit val executionContext: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(User).retrieve(
        Retrievals.externalId and
        Retrievals.confidenceLevel and
        Retrievals.affinityGroup and
        Retrievals.allEnrolments and
        Retrievals.credentials and
        Retrievals.groupIdentifier
    ) {
      case Some(id) ~ cl ~ Some(affinityGroup) ~ enrolments ~ Some(credentials) ~ Some(groupIdentifier) =>
        checkForBothEnrolments(id, request, enrolments).flatMap {
          case None => redirectToInterceptPages(enrolments, affinityGroup).fold {
              val authRequest = AuthenticatedRequest(request, id,
                psaUser(affinityGroup, None, enrolments, credentials.providerId, groupIdentifier))
              successRedirect(affinityGroup, cl, enrolments, authRequest, block)
            } { result => Future.successful(result) }
          case Some(redirect) => Future.successful(redirect)
        }
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))

    } recover handleFailure
  }


  def successRedirect[A](affinityGroup: AffinityGroup,
                         cl: ConfidenceLevel,
                         enrolments: Enrolments,
                         authRequest: AuthenticatedRequest[A],
                         block: AuthenticatedRequest[A] => Future[Result])
                        (implicit hc: HeaderCarrier): Future[Result] = {
    getData(AreYouInUKId, authRequest.externalId).flatMap {
      case _ if alreadyEnrolledInPODS(enrolments) =>
        savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
      case Some(true) if affinityGroup == Organisation =>
        featureToggleConnector.get(FromIvToPdv.asString).map { toggle =>
          toggle.isEnabled
        }.flatMap {
          case true => doManualPDVAndRetrieveNino(authRequest, block)
          case false => doManualIVAndRetrieveNino(authRequest, block)
        }
      case _ =>
        block(authRequest)
    }
  }

  private def checkForBothEnrolments[A](id: String, request: Request[A], enrolments:Enrolments): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    (enrolments.getEnrolment("HMRC-PODS-ORG"), enrolments.getEnrolment("HMRC-PODSPP-ORG")) match {
      case (Some(_), Some(_)) =>
        sessionDataCacheConnector.fetch(id).flatMap { optionJsValue =>
          optionJsValue.map(UserAnswers).flatMap(_.get(AdministratorOrPractitionerId)) match {
            case None => Future.successful(Some(Redirect(config.administratorOrPractitionerUrl)))
            case Some(Practitioner) =>
              Future.successful(Some(Redirect(Call("GET",
                config.cannotAccessPageAsPractitionerUrl(config.localFriendlyUrl(request.uri))))))
            case _ => Future.successful(None)
          }
        }
      case _ => Future.successful(None)
    }
  }

  protected def savePsaIdAndReturnAuthRequest[A](enrolments: Enrolments,
                                                 authRequest: AuthenticatedRequest[A],
                                                 block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    if (alreadyEnrolledInPODS(enrolments)) {
      val psaId = getPSAId(enrolments)
      block(AuthenticatedRequest(authRequest.request, authRequest.externalId, authRequest.user.copy(
        alreadyEnrolledPsaId = Some(psaId))))
    }
    else {
      block(authRequest)
    }
  }


  private def doManualIVAndRetrieveNino[A](authRequest: AuthenticatedRequest[A],
                                           block: AuthenticatedRequest[A] => Future[Result])
                                          (implicit hc: HeaderCarrier): Future[Result] = {

    val id = authRequest.request.getQueryString("journeyId")

    getData(JourneyId, authRequest.externalId).flatMap {
      case Some(journey) =>
        getNinoAndUpdateAuthRequestIV(journey, block, authRequest)
      case _ if id.nonEmpty =>
        userAnswersCacheConnector.save(authRequest.externalId, JourneyId, id.getOrElse("")).flatMap(_ =>
          getNinoAndUpdateAuthRequestIV(id.getOrElse(""), block, authRequest)
        )
      case _ =>
        orgManualIV(authRequest.externalId, authRequest, block)
    }
  }

  private def doManualPDVAndRetrieveNino[A](authRequest: AuthenticatedRequest[A],
                                      block: AuthenticatedRequest[A] => Future[Result])
                                     (implicit hc: HeaderCarrier): Future[Result] = {

    val id = authRequest.request.getQueryString("validationId")

    getData(ValidationId, authRequest.externalId).flatMap {
      case Some(journey) =>
        getNinoAndUpdateAuthRequestPDV(journey, block, authRequest)
      case _ if id.nonEmpty =>
        userAnswersCacheConnector.save(authRequest.externalId, ValidationId, id.getOrElse("")).flatMap(_ =>
          getNinoAndUpdateAuthRequestPDV(id.getOrElse(""), block, authRequest)
        )
      case _ =>
        orgManualPDV(authRequest.externalId, authRequest, block)
    }
  }

  private def getNinoAndUpdateAuthRequestIV[A](journeyId: String,
                                             block: AuthenticatedRequest[A] => Future[Result],
                                             authRequest: AuthenticatedRequest[A])
                                            (implicit hc: HeaderCarrier): Future[Result] = {
    personalDetailsValidationConnector.retrieveNino(journeyId).flatMap {
      case Some(nino) =>
        val updatedAuth = AuthenticatedRequest(
          request = authRequest.request,
          externalId = authRequest.externalId,
          user = authRequest.user.copy(nino = Some(nino))
        )
        block(updatedAuth)
      case _ =>
        userAnswersCacheConnector.remove(authRequest.externalId, JourneyId).flatMap(
          _ => orgManualIV(authRequest.externalId, authRequest, block)
        )
    }
  }

  private def getNinoAndUpdateAuthRequestPDV[A](validationId: String,
                                                block: AuthenticatedRequest[A] => Future[Result],
                                                authRequest: AuthenticatedRequest[A])
                                            (implicit hc: HeaderCarrier): Future[Result] = {
    personalDetailsValidationConnector.retrieveNino(validationId).flatMap {
      case Some(nino) =>
        val updatedAuth = AuthenticatedRequest(
          request = authRequest.request,
          externalId = authRequest.externalId,
          user = authRequest.user.copy(nino = Some(nino))
        )
        block(updatedAuth)
      case _ =>
        userAnswersCacheConnector.remove(authRequest.externalId, ValidationId).flatMap(
          _ => orgManualPDV(authRequest.externalId, authRequest, block)
        )
    }
  }

  private def orgManualIV[A](id: String,
                             authRequest: AuthenticatedRequest[A],
                             block: AuthenticatedRequest[A] => Future[Result])
                            (implicit hc: HeaderCarrier): Future[Result] = {

    getData(RegisterAsBusinessId, id).flatMap {
      case Some(false) =>
        personalDetailsValidationConnector.startRegisterOrganisationAsIndividual(
          config.ukJourneyContinueUrl,
          s"${config.loginContinueUrl}/unauthorised"
        ).map { link =>
          Redirect(s"${config.manualIvUrl}$link")
        }
      case _ =>
        block(authRequest)
    }
  }

  private def orgManualPDV[A](id: String,
                             authRequest: AuthenticatedRequest[A],
                             block: AuthenticatedRequest[A] => Future[Result])
                            (implicit hc: HeaderCarrier): Future[Result] = {

    getData(RegisterAsBusinessId, id).flatMap {
      case Some(false) =>
        val completionURL = config.ukJourneyContinueUrl
        val failureURL = s"${config.loginContinueUrl}"
        val url = s"${config.personalDetailsValidationFrontEnd}" +
          s"/personal-details-validation/start?completionUrl=$completionURL&failureUrl=$failureURL"
        Future.successful(SeeOther(url))
      case _ =>
        block(authRequest)
    }
  }

  private def redirectToInterceptPages(enrolments: Enrolments, affinityGroup: AffinityGroup): Option[Result] = {
    if (isPSP(enrolments) && !isPSA(enrolments)) {
      Some(Redirect(routes.PensionSchemePractitionerController.onPageLoad()))
    } else {
      affinityGroup match {
        case Agent =>
          Some(Redirect(routes.AgentCannotRegisterController.onPageLoad()))
        case Individual if !alreadyEnrolledInPODS(enrolments) =>
          Some(Redirect(routes.UseOrganisationCredentialsController.onPageLoad()))
        case _ =>
          None
      }
    }
  }

  private def getData[A](typedId: TypedIdentifier[A], id: String)(implicit hc: HeaderCarrier, rds: Reads[A]): Future[Option[A]] = {
    userAnswersCacheConnector.fetch(id).map {
      case Some(json) =>
        UserAnswers(json).get(typedId)
      case None =>
        None
    }
  }

  private def handleFailure: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: InsufficientEnrolments =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: InsufficientConfidenceLevel =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAuthProvider =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAffinityGroup =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedCredentialRole =>
      Redirect(routes.UnauthorisedAssistantController.onPageLoad())
    case _: UnauthorizedException =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }

  private def existingPSA(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-PSA-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)

  private def isPSP(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PP-ORG").nonEmpty

  private def isPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PSA-ORG").nonEmpty

  protected def alreadyEnrolledInPODS(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-PODS-ORG").nonEmpty

  protected def userType(affinityGroup: AffinityGroup): UserType = {
    affinityGroup match {
      case Individual =>
        UserType.Individual
      case Organisation =>
        UserType.Organisation
      case _ =>
        throw new UnauthorizedException("Unable to authorise the user")
    }
  }

  protected def psaUser(affinityGroup: AffinityGroup,
                        nino: Option[domain.Nino],
                        enrolments: Enrolments,
                        userId: String,
                        groupIdentifier: String
  ): PSAUser = {
    val psa = existingPSA(enrolments)
    PSAUser(userType(affinityGroup), nino, psa.nonEmpty, psa, None, userId, groupIdentifier)
  }

  protected def getPSAId(enrolments: Enrolments): String =
    enrolments.getEnrolment("HMRC-PODS-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)
      .getOrElse(throw new RuntimeException("PSA ID missing"))
}

trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

class AuthenticationWithNoIV @Inject()(override val authConnector: AuthConnector,
                                       config: FrontendAppConfig,
                                       userAnswersCacheConnector: UserAnswersCacheConnector,
                                       personalDetailsValidationConnector: PersonalDetailsValidationConnector,
                                       sessionDataCacheConnector: SessionDataCacheConnector,
                                       parser: BodyParsers.Default,
                                       featureToggleConnector: FeatureToggleConnector
                                      )(implicit executionContext: ExecutionContext) extends
  FullAuthentication(authConnector, config, userAnswersCacheConnector, personalDetailsValidationConnector,
    sessionDataCacheConnector, parser, featureToggleConnector)

  with AuthorisedFunctions {

  override def successRedirect[A](affinityGroup: AffinityGroup, cl: ConfidenceLevel,
                                  enrolments: Enrolments, authRequest: AuthenticatedRequest[A],
                                  block: AuthenticatedRequest[A] => Future[Result])
                                 (implicit hc: HeaderCarrier): Future[Result] =
    savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
}
