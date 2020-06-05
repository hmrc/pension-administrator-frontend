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
import connectors.MinimalPsaConnector
import models.requests.AuthenticatedRequest
import play.api.mvc.Results._
import play.api.mvc.ActionFilter
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AllowAccessForNonSuspendedUsersAction @Inject()(minimalPsaConnector: MinimalPsaConnector)
                                                     (implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    request.user.alreadyEnrolledPsaId match {
      case None => Future.successful(None)
      case Some(psaId) =>
        minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalDetails =>
          if (minimalDetails.isPsaSuspended) {
            Some(Redirect(controllers.deregister.routes.UnableToStopBeingPsaController.onPageLoad()))
          } else {
            None
          }
        }
    }
  }
}


