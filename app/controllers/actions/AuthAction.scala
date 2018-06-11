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
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core._
import config.FrontendAppConfig
import controllers.routes
import models.UserType.UserType
import models.{PSAUser, UserType}
import models.requests.AuthenticatedRequest
import uk.gov.hmrc.http.UnauthorizedException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.AffinityGroup._

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised().retrieve(
      Retrievals.externalId and
        Retrievals.confidenceLevel and
        Retrievals.affinityGroup and
        Retrievals.nino and
        Retrievals.allEnrolments) {
      case Some(id) ~ cl ~ Some(affinityGroup) ~ nino ~ enrolments =>
        if (alreadyEnrolledInPODS(enrolments) && notConfirmation(request)) {
          Future.successful(Redirect(toggledPage))
        } else if (affinityGroup == Individual && !allowedIndividual(cl)) {
          Future.successful(Redirect(ivUpliftUrl))
        } else {
          block(AuthenticatedRequest(request, id, psaUser(cl, affinityGroup, nino, enrolments)))
        }
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))

    } recover handleFailure
  }

  private def toggledPage: String = {
    if (config.schemeOverviewEnabled) {
      config.schemesOverviewUrl
    } else {
      config.registerSchemeUrl
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
      Redirect(routes.UnauthorisedController.onPageLoad())
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

  private def alreadyEnrolledInPODS(enrolments: Enrolments) =
    enrolments.getEnrolment("HMRC-PODS-ORG").nonEmpty

  private def notConfirmation[A](request: Request[A]): Boolean = {
    val confirmationSeq = Seq(config.confirmationUri, config.duplicateRegUri)
    !confirmationSeq.contains(request.uri)
  }

  private def userType(affinityGroup: AffinityGroup, cl: ConfidenceLevel): UserType = {
    affinityGroup match {
      case Individual if allowedIndividual(cl) =>
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
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
