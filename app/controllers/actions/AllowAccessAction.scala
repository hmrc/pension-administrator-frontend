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

import com.google.inject.Inject
import connectors.MinimalPsaConnector
import models._
import models.requests.AuthenticatedRequest
import play.api.mvc.{ActionFilter, Request, Result}
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AllowAccessAction(
                         mode: Mode,
                         minimalPsaConnector: MinimalPsaConnector
                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    (request.user.alreadyEnrolledPsaId, mode) match {
      case (None, NormalMode | CheckMode) =>
        Future.successful(None)
      case (Some(psaId), UpdateMode | CheckUpdateMode) =>
        minimalPsaConnector.isPsaSuspended(psaId).map{ isSuspended =>
          if(isSuspended) {
            Some(Redirect(controllers.routes.CannotMakeChangesController.onPageLoad()))
          } else {
            None
          }
        }
      case (Some(_), NormalMode) if pagesAfterEnrolment(request) =>
        Future.successful(None)
      case (Some(_), NormalMode | CheckMode) =>
        Future.successful(Some(Redirect(controllers.routes.InterceptPSAController.onPageLoad())))
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

class AllowAccessActionProviderImpl @Inject() (minimalPsaConnector: MinimalPsaConnector)(implicit executionContext: ExecutionContext)
  extends AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction = {
    new AllowAccessAction(mode, minimalPsaConnector)
  }
}

trait AllowAccessActionProvider {
  def apply(mode: Mode): AllowAccessAction
}
