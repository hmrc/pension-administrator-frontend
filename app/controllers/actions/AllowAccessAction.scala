/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.MinimalPsaConnector
import connectors.cache.UserAnswersCacheConnector
import identifiers.RLSFlagId
import models._
import models.requests.AuthenticatedRequest
import play.api.mvc.Call
import play.api.mvc.ActionFilter
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AllowAccessAction(
                         mode: Mode,
                         minimalPsaConnector: MinimalPsaConnector,
                         config: FrontendAppConfig,
                         userAnswersCacheConnector: UserAnswersCacheConnector
                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  protected def redirects(externalId: String, psaId:String)(implicit hc: HeaderCarrier):Future[Option[Result]] = {
    minimalPsaConnector.getMinimalPsaDetails(psaId).flatMap { minimalPSA =>
      if (minimalPSA.isPsaSuspended) {
        Future.successful(Some(Redirect(controllers.routes.CannotMakeChangesController.onPageLoad())))
      } else if (minimalPSA.rlsFlag) {
        println("\n>>>>TRTRTR")
        userAnswersCacheConnector.save(externalId, RLSFlagId, true).map { _ =>
          Option(Redirect(controllers.routes.UpdateContactAddressController.onPageLoad()))
        }
      } else {
        Future.successful(None)
      }
    }
  }

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    (request.user.alreadyEnrolledPsaId, mode) match {
      case (None, NormalMode | CheckMode) =>
        Future.successful(None)
      case (Some(psaId), UpdateMode | CheckUpdateMode) =>
        redirects(request.externalId, psaId)
      case (Some(_), NormalMode) if pagesAfterEnrolment(request) =>
        Future.successful(None)
      case (Some(_), NormalMode | CheckMode) =>
        Future.successful(Some(Redirect( Call("GET", config.schemesOverviewUrl) )))
      case _ =>
        Future.successful(Some(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
    }
  }

  private def pagesAfterEnrolment[A](request: Request[A]): Boolean = {
    val confirmationSeq = Seq(controllers.register.routes.ConfirmationController.onPageLoad().url,
      controllers.register.routes.DuplicateRegistrationController.onPageLoad().url)
    confirmationSeq.contains(request.uri)
  }
}

class AllowAccessActionNoUpdateContactAddress(
  mode: Mode,
  minimalPsaConnector: MinimalPsaConnector,
  config: FrontendAppConfig,
  userAnswersCacheConnector: UserAnswersCacheConnector
)(implicit override val executionContext: ExecutionContext) extends AllowAccessAction(mode, minimalPsaConnector, config, userAnswersCacheConnector) {
  override protected def redirects(externalId: String, psaId:String)(implicit hc: HeaderCarrier):Future[Option[Result]] = {
    minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalPSA =>
      if (minimalPSA.isPsaSuspended) {
        Some(Redirect(controllers.routes.CannotMakeChangesController.onPageLoad()))
      } else {
        None
      }
    }
  }
}

class AllowAccessActionProviderNoUpdateContactAddressImpl @Inject() (
  minimalPsaConnector: MinimalPsaConnector, config: FrontendAppConfig,
  userAnswersCacheConnector: UserAnswersCacheConnector)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessActionNoUpdateContactAddress(mode, minimalPsaConnector, config, userAnswersCacheConnector)
  }
}

class AllowAccessActionProviderImpl @Inject() (minimalPsaConnector: MinimalPsaConnector,
  config: FrontendAppConfig, userAnswersCacheConnector: UserAnswersCacheConnector)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessAction(mode, minimalPsaConnector, config, userAnswersCacheConnector)
  }
}

trait AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction
}
