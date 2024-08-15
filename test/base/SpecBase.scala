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

package base

import config.FrontendAppConfig
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Environment}

trait SpecBase
  extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with BeforeAndAfterAll
    with MockitoSugar {

  override lazy val app: Application =
    new GuiceApplicationBuilder().build()

  override def afterAll(): Unit = {
    System.gc()
    super.afterAll()
  }
  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = inject[FrontendAppConfig]

  def environment: Environment = inject[Environment]

  def messagesApi: MessagesApi = inject[MessagesApi]

  def controllerComponents: MessagesControllerComponents = inject[MessagesControllerComponents]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def messages: Messages = controllerComponents.messagesApi.preferred(fakeRequest)
}

object SpecBase extends SpecBase
