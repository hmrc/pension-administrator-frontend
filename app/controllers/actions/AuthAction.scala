/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes
import identifiers.register.AreYouInUKId
import identifiers.{PsaId => UserPsaId}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig, userAnswersCacheConnector: UserAnswersCacheConnector)
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

        redirectToInterceptPages(enrolments, request).fold(
          result => Future.successful(result),
          _ => {
            val authRequest = AuthenticatedRequest(request, id, psaUser(cl, affinityGroup, nino, enrolments))

            if (affinityGroup == Individual) {
              areYouInUK(id).flatMap {
                case Some(true) if !allowedIndividual(cl) =>
                  Future.successful(Redirect(ivUpliftUrl))
                case _ =>
                  block(authRequest)
              }
            } else {
              if (alreadyEnrolledInPODS(enrolments)) {
                userAnswersCacheConnector.save(id, UserPsaId, getPSAId(enrolments)).flatMap {
                  _ => block(authRequest)
                }
              } else {
                block(authRequest)
              }
            }
          }
        )
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))

    } recover handleFailure
  }

  private def redirectToInterceptPages[A](enrolments: Enrolments, request: Request[A]) = {
    if (alreadyEnrolledInPODS(enrolments) && notNewRegPages(request)) {
      Left(Redirect(routes.InterceptPSAController.onPageLoad()))
    } else if (isPSP(enrolments) && !isPSA(enrolments)) {
      Left(Redirect(routes.PensionSchemePractitionerController.onPageLoad()))
    } else {
      Right(())
    }
  }

  private def areYouInUK(id: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    userAnswersCacheConnector.fetch(id).map {
      case Some(json) =>
        val userAnswers = UserAnswers(json)
        userAnswers.get(AreYouInUKId)
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

  private def ivUpliftUrl: String = s"${config.ivUpliftUrl}?origin=PODS&" +
    s"completionURL=${URLEncoder.encode(config.loginContinueUrl, "UTF-8")}&" +
    s"failureURL=${URLEncoder.encode(s"${config.loginContinueUrl}/unauthorised", "UTF-8")}" +
    s"&confidenceLevel=${ConfidenceLevel.L200.level}"

  private def allowedIndividual(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L200) >= 0

  private def allowedOrganisation(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L50) >= 0

  private def existingPSA(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-PSA-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)

  private def isPSP(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PP-ORG").nonEmpty

  private def isPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PSA-ORG").nonEmpty

  protected def alreadyEnrolledInPODS(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-PODS-ORG").nonEmpty

  private def notNewRegPages[A](request: Request[A]): Boolean = {
    val confirmationSeq = Seq(config.confirmationUri, config.duplicateRegUri, config.registeredPsaDetailsUri)
    !confirmationSeq.contains(request.uri)
  }

  private def userType(affinityGroup: AffinityGroup, cl: ConfidenceLevel): UserType = {
    affinityGroup match {
      case Individual =>
        UserType.Individual
      case Organisation if allowedOrganisation(cl) =>
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

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
