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

package controllers.register.partnership

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, FakeAuthAction}
import identifiers.register.partnership.MoreThanTenPartnersId
import models.{Mode, NormalMode}
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{Message, MoreThanTenViewModel}

class MoreThanTenPartnersControllerSpec extends ControllerSpecBase {

  import MoreThanTenPartnersControllerSpec._

  private def viewModel(mode: Mode): MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenPartners.title",
      heading = Message("moreThanTenPartners.heading"),
      hint = "moreThanTenPartners.hint",
      postCall = routes.MoreThanTenPartnersController.onSubmit(mode),
      id = MoreThanTenPartnersId
    )

  "MoreThanTenPartnersController" must {

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(this).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(this).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "check the view model is correct" in {
      val expected: MoreThanTenViewModel = MoreThanTenViewModel(
        title = "moreThanTenPartners.title",
        heading = Message("moreThanTenPartners.heading"),
        hint = "moreThanTenPartners.hint",
        postCall = routes.MoreThanTenPartnersController.onSubmit(NormalMode),
        id = MoreThanTenPartnersId
      )

      val actual = controller(this).viewModel(NormalMode)

      actual mustBe expected
    }
  }
}

object MoreThanTenPartnersControllerSpec {

  def controller(base: ControllerSpecBase): MoreThanTenPartnersController =
    new MoreThanTenPartnersController(
      base.frontendAppConfig,
      base.messagesApi,
      FakeUserAnswersCacheConnector,
      FakeNavigator,
      FakeAuthAction,
      base.dontGetAnyData,
      new DataRequiredActionImpl
    )
}
