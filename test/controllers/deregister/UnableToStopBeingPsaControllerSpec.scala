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

package controllers.deregister

import controllers.ControllerSpecBase
import controllers.actions.FakeAuthAction
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.deregister.unableToStopBeingPsa

class UnableToStopBeingPsaControllerSpec extends ControllerSpecBase with ScalaFutures {
  
  import UnableToStopBeingPsaControllerSpec._

  "UnableToStopBeingPsaController" must {
    "return OK and the correct view for a GET" in {

      val result = controller.onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}

object UnableToStopBeingPsaControllerSpec extends ControllerSpecBase {

  val view: unableToStopBeingPsa = app.injector.instanceOf[unableToStopBeingPsa]

  private def controller =
    new UnableToStopBeingPsaController(
      frontendAppConfig,
      FakeAuthAction(),
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(): String =
    view()(fakeRequest, messages).toString

}