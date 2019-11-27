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

package base

import akka.stream.Materializer
import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Injector, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Environment}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterAll {

  override lazy val app: Application = new GuiceApplicationBuilder().build()

  protected def applicationBuilder(data: DataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[FrontendAppConfig].toInstance(frontendAppConfig),
        bind[DataRetrievalAction].toInstance(data),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[MessagesControllerComponents].to(messagesControllerComponents)
      )

  def messagesControllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = inject[FrontendAppConfig]

  implicit val mat: Materializer = inject[Materializer]

  def environment: Environment = inject[Environment]

  def messagesApi: MessagesApi = inject[MessagesApi]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def messages: Messages = messagesControllerComponents.messagesApi.preferred(fakeRequest)

  def appRunning(): Unit = app

  override def afterAll(): Unit = {
    super.afterAll()
    app.stop()
  }
}
