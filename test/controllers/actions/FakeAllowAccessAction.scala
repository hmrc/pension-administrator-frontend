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
import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import models.MinimalPSA
import models.requests.AuthenticatedRequest
import models.{Mode, NormalMode}
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext
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
  ), config, FakeAllowAccessAction.fakeUserAnswersCacheConnector) {
  override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
    Future.successful(None)
}

object FakeAllowAccessAction {
  def fakeUserAnswersCacheConnector: UserAnswersCacheConnector = new UserAnswersCacheConnector {
    override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I,
      value: A)
      (implicit fmt: Format[A], executionContext: ExecutionContext,
        hc: HeaderCarrier) = ???

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
      (implicit executionContext: ExecutionContext,
        hc: HeaderCarrier) = ???

    override def fetch(cacheId: String)
      (implicit executionContext: ExecutionContext,
        hc: HeaderCarrier) = ???

    override def upsert(cacheId: String, value: JsValue)
      (implicit executionContext: ExecutionContext,
        hc: HeaderCarrier) = ???

    override def removeAll(cacheId: String)
      (implicit executionContext: ExecutionContext,
        hc: HeaderCarrier) = ???
  }
}

case class FakeAllowAccessProvider(mode: Mode = NormalMode, config: FrontendAppConfig) extends AllowAccessActionProvider {
  override def apply(mode: Mode): AllowAccessAction =
    new FakeAllowAccessAction(mode, config)
}

