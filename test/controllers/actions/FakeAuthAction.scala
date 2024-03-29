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

import base.SpecBase.controllerComponents
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.mvc.{AnyContent, BodyParser, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

case class FakeAuthAction(userType: UserType, psaId:String = "test psa id") extends AuthAction {
  val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
  block(AuthenticatedRequest(request, "id", PSAUser(userType, None, isExistingPSA = false, None, Some(psaId))))
}

object FakeAuthAction extends AuthAction {
  val externalId: String = "id"
  private val defaultPsaId: String = "A0000000"
  def apply(): AuthAction = {
    new AuthAction {
      val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
      implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
      override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
        block(AuthenticatedRequest(request, externalId, PSAUser(UserType.Organisation, None, isExistingPSA = false, None, Some(defaultPsaId))))
    }
  }

  val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, externalId, PSAUser(UserType.Organisation, None, isExistingPSA = false, Some("test Psa id"))))

}
