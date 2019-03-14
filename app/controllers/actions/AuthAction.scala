/*
 * Copyright 2019 HM Revenue & Customs
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

import java.net.URLEncoder

import com.google.inject.Inject
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.{IdentityVerificationConnector, MinimalPsaConnector, UserAnswersCacheConnector}
import controllers.routes
import identifiers.register.{AreYouInUKId, RegisterAsBusinessId}
import identifiers.{JourneyId, TypedIdentifier}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.libs.json.Reads
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.Toggles.IsManualIVEnabled
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FullAuthentication @Inject()(override val authConnector: AuthConnector,
                                   config: FrontendAppConfig,
                                   fs: FeatureSwitchManagementService,
                                   userAnswersCacheConnector: UserAnswersCacheConnector,
                                   ivConnector: IdentityVerificationConnector,
                                   minimalPsaConnector: MinimalPsaConnector
                                  )
                                  (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(User or Admin).retrieve(
      Retrievals.externalId and
        Retrievals.confidenceLevel and
        Retrievals.affinityGroup and
        Retrievals.nino and
        Retrievals.allEnrolments) {
      case Some(id) ~ cl ~ Some(affinityGroup) ~ nino ~ enrolments =>
        redirectToInterceptPages(enrolments, request, cl, affinityGroup, id).fold {
          val authRequest = AuthenticatedRequest(request, id, psaUser(cl, affinityGroup, nino, enrolments))
          successRedirect(affinityGroup, cl, enrolments, authRequest, block)
        } { result => Future.successful(result) }
      case _ =>
        Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))

    } recover handleFailure
  }

  def successRedirect[A](affinityGroup: AffinityGroup, cl: ConfidenceLevel, enrolments: Enrolments, authRequest: AuthenticatedRequest[A],
                         block: AuthenticatedRequest[A] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    if (fs.get(IsManualIVEnabled)) {
      successRedirectIVEnable(affinityGroup, cl, enrolments, authRequest, block)
    } else {
      successRedirectIVDisable(affinityGroup, cl, enrolments, authRequest, block)
    }
  }

  private def successRedirectIVDisable[A](affinityGroup: AffinityGroup, cl: ConfidenceLevel,
                                          enrolments: Enrolments, authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result])
                                         (implicit hc: HeaderCarrier): Future[Result] =
    if (affinityGroup == Individual) {
      getData(AreYouInUKId, authRequest.externalId).flatMap {
        case Some(true) if !allowedIndividual(cl) =>
          Future.successful(Redirect(ivUpliftUrl))
        case _ =>
          savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
      }
    } else {
      savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
    }

  private def successRedirectIVEnable[A](affinityGroup: AffinityGroup, cl: ConfidenceLevel,
                                         enrolments: Enrolments, authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result])
                                        (implicit hc: HeaderCarrier) = {

    getData(AreYouInUKId, authRequest.externalId).flatMap {
      case Some(true) if affinityGroup == Individual && !allowedIndividual(cl) =>
        Future.successful(Redirect(ivUpliftUrl))
      case Some(true) if affinityGroup == Organisation =>
        doManualIVAndRetrieveNino(authRequest, enrolments, block)
      case _ =>
        savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
    }
  }

  private def doManualIVAndRetrieveNino[A](authRequest: AuthenticatedRequest[A], enrolments: Enrolments,
                                           block: AuthenticatedRequest[A] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    val journeyId = authRequest.request.getQueryString("journeyId")
    getData(JourneyId, authRequest.externalId).flatMap {
      case Some(journey) =>
        getNinoAndUpdateAuthRequest(journey, enrolments, block, authRequest)
      case _ if journeyId.nonEmpty =>
        userAnswersCacheConnector.save(authRequest.externalId, JourneyId, journeyId.getOrElse("")).flatMap(_ =>
          getNinoAndUpdateAuthRequest(journeyId.getOrElse(""), enrolments, block, authRequest)
        )
      case _ =>
        orgManualIV(authRequest.externalId, enrolments, authRequest, block)
    }
  }

  private def getNinoAndUpdateAuthRequest[A](journeyId: String, enrolments: Enrolments, block: AuthenticatedRequest[A] => Future[Result],
                                             authRequest: AuthenticatedRequest[A])(implicit hc: HeaderCarrier): Future[Result] = {
    ivConnector.retrieveNinoFromIV(journeyId).flatMap {
      case Some(nino) =>
        val updatedAuth = AuthenticatedRequest(authRequest.request, authRequest.externalId, authRequest.user.copy(nino = Some(nino)))
        savePsaIdAndReturnAuthRequest(enrolments, updatedAuth, block)
      case _ =>
        orgManualIV(authRequest.externalId, enrolments, authRequest, block)
    }
  }

  private def orgManualIV[A](id: String,
                             enrolments: Enrolments, authRequest: AuthenticatedRequest[A],
                             block: AuthenticatedRequest[A] => Future[Result])(implicit hc: HeaderCarrier) = {

    getData(RegisterAsBusinessId, id).flatMap {
      case Some(false) =>
        ivConnector.startRegisterOrganisationAsIndividual(
          config.ukJourneyContinueUrl,
          s"${config.loginContinueUrl}/unauthorised"
        ).map { link =>
          Redirect(s"${config.manualIvUrl}$link")
        }
      case _ =>
        savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
    }
  }


  protected def savePsaIdAndReturnAuthRequest[A](enrolments: Enrolments, authRequest: AuthenticatedRequest[A],
                                                 block: AuthenticatedRequest[A] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    if (alreadyEnrolledInPODS(enrolments)) {
      val psaId = getPSAId(enrolments)
      minimalPsaConnector.isPsaSuspended(psaId).flatMap { isPsaSuspended =>
        block(AuthenticatedRequest(authRequest.request, authRequest.externalId, authRequest.user.copy(
          alreadyEnrolledPsaId = Some(psaId), isPSASuspended = isPsaSuspended)))
      }
    }
    else {
      block(authRequest)
    }
  }


  private def redirectToInterceptPages[A](enrolments: Enrolments, request: Request[A],
                                          cl: ConfidenceLevel, affinityGroup: AffinityGroup, id: String)(implicit hc: HeaderCarrier) = {
    if (isPSP(enrolments) && !isPSA(enrolments)) {
      Some((Redirect(routes.PensionSchemePractitionerController.onPageLoad())))
    } else if (affinityGroup == Agent) {
      Some((Redirect(routes.AgentCannotRegisterController.onPageLoad)))
    }
    else {
      None
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

  private def ivUpliftUrl: String = {
    s"${config.ivUpliftUrl}?origin=PODS&" +
      s"completionURL=${URLEncoder.encode(config.ukJourneyContinueUrl, "UTF-8")}&" +
      s"failureURL=${URLEncoder.encode(s"${config.loginContinueUrl}/unauthorised", "UTF-8")}" +
      s"&confidenceLevel=${ConfidenceLevel.L200.level}"
  }

  private def allowedIndividual(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L200) >= 0

  private def existingPSA(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-PSA-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)

  private def isPSP(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PP-ORG").nonEmpty

  private def isPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PSA-ORG").nonEmpty

  protected def alreadyEnrolledInPODS(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-PODS-ORG").nonEmpty

  private def userType(affinityGroup: AffinityGroup, cl: ConfidenceLevel): UserType = {
    affinityGroup match {
      case Individual =>
        UserType.Individual
      case Organisation =>
        UserType.Organisation
      case _ =>
        throw new UnauthorizedException("Unable to authorise the user")
    }
  }

  private def psaUser(cl: ConfidenceLevel, affinityGroup: AffinityGroup,
                      nino: Option[String], enrolments: Enrolments): PSAUser = {
    val psa = existingPSA(enrolments)
    PSAUser(userType(affinityGroup, cl), nino, psa.nonEmpty, psa)
  }

  private def getPSAId(enrolments: Enrolments): String =
    enrolments.getEnrolment("HMRC-PODS-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)
      .getOrElse(throw new RuntimeException("PSA ID missing"))
}

class AuthenticationWithNoConfidence @Inject()(override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               fs: FeatureSwitchManagementService,
                                               userAnswersCacheConnector: UserAnswersCacheConnector,
                                               identityVerificationConnector: IdentityVerificationConnector
                                              )(implicit ec: ExecutionContext)
  extends FullAuthentication(authConnector, config, fs, userAnswersCacheConnector, identityVerificationConnector) with AuthorisedFunctions {


  override def successRedirect[A](affinityGroup: AffinityGroup, cl: ConfidenceLevel,
                                  enrolments: Enrolments, authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result])
                                 (implicit hc: HeaderCarrier) =

    savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
}

trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
