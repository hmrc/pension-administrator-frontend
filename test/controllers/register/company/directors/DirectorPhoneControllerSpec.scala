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

package controllers.register.company.directors

import controllers.ControllerSpecBase
import models.NormalMode
import play.api.test.Helpers._

class DirectorPhoneControllerSpec extends ControllerSpecBase {

  "DirectorPhoneController" must {

    "redirect to Session Expired for a GET if no existing data is found" in {
      running(
        _.overrides(modules(dontGetAnyData): _*)
      ) {
        app =>
          val controller = app.injector.instanceOf[DirectorPhoneController]
          val result = controller.onPageLoad(NormalMode, index = 0)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      running(
        _.overrides(modules(dontGetAnyData): _*)
      ) {
        app =>
          val controller = app.injector.instanceOf[DirectorPhoneController]
          val result = controller.onSubmit(NormalMode, index = 0)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
