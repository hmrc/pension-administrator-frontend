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
import models._
import models.requests.AuthenticatedRequest
import play.api.mvc.Call
import play.api.mvc.{Request, Result, ActionFilter}
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{Future, ExecutionContext}

class AllowAccessAction(
                         mode: Mode,
                         minimalPsaConnector: MinimalPsaConnector,
                         config: FrontendAppConfig
                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  protected def redirects(psaId:String)(implicit hc: HeaderCarrier):Future[Option[Result]] = {
    minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalPSA =>
      if (minimalPSA.isPsaSuspended) {
        Some(Redirect(controllers.routes.CannotMakeChangesController.onPageLoad()))
      } else if (minimalPSA.rlsFlag) {
        Some(Redirect(controllers.routes.UpdateContactAddressController.onPageLoad()))
      } else {
        None
      }
    }
  }

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    (request.user.alreadyEnrolledPsaId, mode) match {
      case (None, NormalMode | CheckMode) =>
        Future.successful(None)
      case (Some(psaId), UpdateMode | CheckUpdateMode) =>
        redirects(psaId)
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

class AllowAccessActionNoRLSCheck(
  mode: Mode,
  minimalPsaConnector: MinimalPsaConnector,
  config: FrontendAppConfig
)(implicit override val executionContext: ExecutionContext) extends AllowAccessAction(mode, minimalPsaConnector, config) {
  override protected def redirects(psaId:String)(implicit hc: HeaderCarrier):Future[Option[Result]] = {
    minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalPSA =>
      if (minimalPSA.isPsaSuspended) {
        Some(Redirect(controllers.routes.CannotMakeChangesController.onPageLoad()))
      } else {
        None
      }
    }
  }
}

class AllowAccessActionNoSuspendedCheck(
  mode: Mode,
  minimalPsaConnector: MinimalPsaConnector,
  config: FrontendAppConfig
)(implicit override val executionContext: ExecutionContext) extends AllowAccessAction(mode, minimalPsaConnector, config) {
  override protected def redirects(psaId:String)(implicit hc: HeaderCarrier):Future[Option[Result]] = {
    minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalPSA =>
      if (minimalPSA.rlsFlag) {
        Some(Redirect(controllers.routes.UpdateContactAddressController.onPageLoad()))
      } else {
        None
      }
    }
  }
}

class AllowAccessActionProviderNoRLSCheckImpl @Inject() (
  minimalPsaConnector: MinimalPsaConnector, config: FrontendAppConfig)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessActionNoRLSCheck(mode, minimalPsaConnector, config)
  }
}

class AllowAccessActionProviderNoSuspendedCheckImpl @Inject() (
  minimalPsaConnector: MinimalPsaConnector, config: FrontendAppConfig)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessActionNoSuspendedCheck(mode, minimalPsaConnector, config)
  }
}

class AllowAccessActionProviderImpl @Inject() (minimalPsaConnector: MinimalPsaConnector, config: FrontendAppConfig)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessAction(mode, minimalPsaConnector, config)
  }
}

trait AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction
}
