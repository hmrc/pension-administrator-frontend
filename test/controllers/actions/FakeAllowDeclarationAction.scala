/*
 * Copyright 2023 HM Revenue & Customs
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

import models.requests.OptionalDataRequest
import models.{Mode, NormalMode}
import play.api.mvc.Result
import utils.dataCompletion.DataCompletion

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeAllowDeclarationAction(mode: Mode) extends AllowDeclarationAction(mode, new DataCompletion) {

  override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] =
    Future.successful(None)
}

case class FakeAllowDeclarationActionProvider(mode: Mode = NormalMode) extends AllowDeclarationActionProvider {
  override def apply(mode: Mode): AllowDeclarationAction =
    new FakeAllowDeclarationAction(mode)
}

