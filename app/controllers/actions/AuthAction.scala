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

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core._
import config.FrontendAppConfig
import controllers.routes
import models.UserType
import models.requests.AuthenticatedRequest
import uk.gov.hmrc.http.UnauthorizedException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.AffinityGroup._

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  private def allowedIndividual(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L200) >= 0

  private def allowedCompany(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L50) >= 0

  private def isExistingPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-PSA-ORG").nonEmpty

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised().retrieve(
      Retrievals.externalId and
        Retrievals.confidenceLevel and
        Retrievals.affinityGroup and
        Retrievals.authorisedEnrolments) {

      case Some(id) ~ cl ~ Some(Individual) ~ enrolments if (allowedIndividual(cl)) =>
        block(AuthenticatedRequest(request, id, UserType.Individual, isExistingPSA(enrolments)))

      case Some(id) ~ cl ~ Some(Organisation) ~ enrolments if (allowedCompany(cl)) =>
        block(AuthenticatedRequest(request, id, UserType.Organisation, isExistingPSA(enrolments)))

      case _ =>
        throw new UnauthorizedException("Unable to authorise the user")

    } recover handleFailure
  }

  private def handleFailure: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: InsufficientEnrolments =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: InsufficientConfidenceLevel =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAuthProvider =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedAffinityGroup =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    case _: UnsupportedCredentialRole =>
      Redirect(routes.UnauthorisedController.onPageLoad)
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
