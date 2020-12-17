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

import config.FrontendAppConfig
import models.MinimalPSA
import models.requests.AuthenticatedRequest
import models.{Mode, NormalMode}
import play.api.mvc.Result

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private class FakeAllowAccessAction(mode: Mode, config: FrontendAppConfig) extends AllowAccessAction(
  mode,
  FakeMinimalPsaConnector(
    MinimalPSA(
      email = "a@a.c",
      isPsaSuspended = true,
      organisationName = None,
      individualDetails = None,
      rlsFlag = false
    )
  ), config) {
  override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
    Future.successful(None)
}

case class FakeAllowAccessProvider(mode: Mode = NormalMode, config: FrontendAppConfig) extends AllowAccessActionProvider {
  override def apply(mode: Mode): AllowAccessAction =
    new FakeAllowAccessAction(mode, config)
}

