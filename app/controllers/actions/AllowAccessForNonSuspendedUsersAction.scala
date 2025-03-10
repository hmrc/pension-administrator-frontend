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
import connectors.MinimalPsaConnector
import models.requests.AuthenticatedRequest
import play.api.mvc.{Result, ActionFilter}
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{Future, ExecutionContext}

class AllowAccessForNonSuspendedUsersAction @Inject()(minimalPsaConnector: MinimalPsaConnector, config: FrontendAppConfig)
                                                     (implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.user.alreadyEnrolledPsaId match {
      case None => Future.successful(None)
      case Some(_) =>
        minimalPsaConnector.getMinimalPsaDetails().map { minimalPSA =>
            if(minimalPSA.isPsaSuspended) {
              Some(Redirect(controllers.deregister.routes.UnableToStopBeingPsaController.onPageLoad))
            } else if (minimalPSA.deceasedFlag) {
              Some(Redirect(config.youMustContactHMRCUrl))
            } else if (minimalPSA.rlsFlag) {
              Some(Redirect(controllers.routes.UpdateContactAddressController.onPageLoad))
            } else {
              None
            }
        }
    }
  }
}


