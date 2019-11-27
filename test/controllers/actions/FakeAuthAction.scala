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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, BodyParser, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, DefaultMessagesControllerComponents, MessagesControllerComponents, PlayBodyParsers, Request, Result}
import play.api.test.Helpers.{stubBodyParser, stubLangs, stubMessagesApi, stubPlayBodyParsers}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

case class FakeAuthAction(userType: UserType, psaId:String = "test psa id") extends AuthAction {
  val parser: BodyParser[AnyContent] = stubMessagesControllerComponents().parsers.defaultBodyParser
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
  block(AuthenticatedRequest(request, "id", PSAUser(userType, None, isExistingPSA = false, None, Some(psaId))))
}

object FakeAuthAction extends AuthAction {

  implicit val sys = ActorSystem("MyTest")
  implicit final val materializer : Materializer = ActorMaterializer()

  def stubMessagesControllerComponents(
                                        bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
                                        playBodyParsers: PlayBodyParsers   = stubPlayBodyParsers(materializer),
                                        messagesApi: MessagesApi           = stubMessagesApi(),
                                        langs: Langs                       = stubLangs(),
                                        fileMimeTypes: FileMimeTypes       = new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
                                        executionContext: ExecutionContext = ExecutionContext.global): MessagesControllerComponents =
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(bodyParser, messagesApi)(executionContext),
      DefaultActionBuilder(bodyParser)(executionContext),
      playBodyParsers,
      messagesApi,
      langs,
      fileMimeTypes,
      executionContext
    )


  val parser: BodyParser[AnyContent] = stubMessagesControllerComponents().parsers.defaultBodyParser
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, externalId, PSAUser(UserType.Organisation, None, isExistingPSA = false, Some("test Psa id"))))

  val externalId: String = "id"
}
