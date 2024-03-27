/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.SessionDataCacheConnector
import controllers.routes
import identifiers.AdministratorOrPractitionerId
import models.AdministratorOrPractitioner.Practitioner
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

protected class FullAuthentication @Inject()(override val authConnector: AuthConnector,
                                   config: FrontendAppConfig,
                                   sessionDataCacheConnector: SessionDataCacheConnector,
                                   val parser: BodyParsers.Default,
                                   minimalConfidenceLevel: Option[ConfidenceLevel])
                                  (implicit val executionContext: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val auth = minimalConfidenceLevel.map(minimalConfidenceLevel =>
      authorised(User and minimalConfidenceLevel)
    ).getOrElse(authorised(User))

    auth.retrieve(
      Retrievals.externalId and
        Retrievals.affinityGroup and
        Retrievals.allEnrolments and
        Retrievals.credentials and
        Retrievals.groupIdentifier and
        Retrievals.nino
    ) {
      case Some(id) ~ Some(affinityGroup) ~ enrolments ~ Some(credentials) ~ Some(groupIdentifier) ~ nino =>
        checkForBothEnrolments(id, request, enrolments).flatMap {
          case None => redirectToInterceptPages(enrolments, affinityGroup).fold {

            val psa = existingPSA(enrolments)
            val psaUser = PSAUser(
              userType(affinityGroup),
              nino.map(uk.gov.hmrc.domain.Nino(_)),
              psa.nonEmpty,
              psa,
              None,
              credentials.providerId, groupIdentifier)

            val authRequest = AuthenticatedRequest(request, id, psaUser)
            successRedirect(enrolments, authRequest, block)
          } { result => Future.successful(result) }
          case Some(redirect) => Future.successful(redirect)
        }
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))

    } recover handleFailure(request)
  }


  def successRedirect[A](enrolments: Enrolments,
                         authRequest: AuthenticatedRequest[A],
                         block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    if(alreadyEnrolledInPODS(enrolments)) {
      savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
    } else {
      block(authRequest)
    }
  }

  private def checkForBothEnrolments[A](id: String, request: Request[A], enrolments: Enrolments): Future[Option[Result]] = {
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

  private def redirectToInterceptPages(enrolments: Enrolments, affinityGroup: AffinityGroup): Option[Result] = {
    if (isPSP(enrolments) && !isPSA(enrolments)) {
      Some(Redirect(routes.PensionSchemePractitionerController.onPageLoad()))
    } else {
      affinityGroup match {
        case Agent =>
          Some(Redirect(routes.AgentCannotRegisterController.onPageLoad))
        case Individual if !alreadyEnrolledInPODS(enrolments) =>
          Some(Redirect(routes.UseOrganisationCredentialsController.onPageLoad))
        case _ =>
          None
      }
    }
  }

  private def handleFailure(request: RequestHeader): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: InsufficientEnrolments =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: InsufficientConfidenceLevel =>
      val completionURL = RedirectUrl(request.uri)
      val failureURL = RedirectUrl(controllers.routes.UnauthorisedController.onPageLoad.url)
      val url = config.identityValidationFrontEndEntry(completionURL, failureURL)
      SeeOther(url)
    case _: UnsupportedAuthProvider =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAffinityGroup =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedCredentialRole =>
      Redirect(routes.UnauthorisedAssistantController.onPageLoad())
    case _: UnauthorizedException =>
      Redirect(routes.UnauthorisedController.onPageLoad)
  }

  private def existingPSA(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-PSA-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)

  private def isPSP(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PP-ORG").nonEmpty

  private def isPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PSA-ORG").nonEmpty

  private def alreadyEnrolledInPODS(enrolments: Enrolments): Boolean =
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

  protected def getPSAId(enrolments: Enrolments): String =
    enrolments.getEnrolment("HMRC-PODS-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)
      .getOrElse(throw new RuntimeException("PSA ID missing"))
}

trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

class AuthenticationAction @Inject()(override val authConnector: AuthConnector,
                                       config: FrontendAppConfig,
                                       sessionDataCacheConnector: SessionDataCacheConnector,
                                       parser: BodyParsers.Default
                                      )(implicit executionContext: ExecutionContext) extends
  FullAuthentication(authConnector, config, sessionDataCacheConnector, parser, None)

class AuthenticationWithIV @Inject()(override val authConnector: AuthConnector,
                                     config: FrontendAppConfig,
                                     sessionDataCacheConnector: SessionDataCacheConnector,
                                     parser: BodyParsers.Default
                                    )(implicit executionContext: ExecutionContext) extends
  FullAuthentication(authConnector, config, sessionDataCacheConnector, parser, Some(ConfidenceLevel.L250))

class AuthenticationWithNoIV @Inject()(override val authConnector: AuthConnector,
                                       config: FrontendAppConfig,
                                       sessionDataCacheConnector: SessionDataCacheConnector,
                                       parser: BodyParsers.Default
                                      )(implicit executionContext: ExecutionContext) extends
  FullAuthentication(authConnector, config, sessionDataCacheConnector, parser, None)

  with AuthorisedFunctions {

  override def successRedirect[A](enrolments: Enrolments, authRequest: AuthenticatedRequest[A],
                                  block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    savePsaIdAndReturnAuthRequest(enrolments, authRequest, block)
}
