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

import models.{CheckMode, Mode, NormalMode, UpdateMode}
import models.requests.AuthenticatedRequest
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results._

import scala.concurrent.Future

class AllowAccessAction(mode:Mode) extends ActionFilter[AuthenticatedRequest]{

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {

    (request.user.alreadyEnrolledPsaId, mode) match {
      case (None, NormalMode | CheckMode) =>  Future.successful(None)
      case (Some(_), UpdateMode) =>  Future.successful(None)
      case (Some(_), NormalMode | CheckMode) =>  Future.successful(Some(Redirect(controllers.routes.InterceptPSAController.onPageLoad())))
      case _ =>  Future.successful(Some(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
    }
  }
}

class AllowAccessActionProviderImpl extends AllowAccessActionProvider{
  def apply(mode:Mode): AllowAccessAction = {
    new AllowAccessAction(mode)
  }
}

trait AllowAccessActionProvider{
  def apply(mode:Mode) : AllowAccessAction
}