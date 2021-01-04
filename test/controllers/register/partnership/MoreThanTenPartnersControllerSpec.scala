/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, FakeAllowAccessProvider, FakeAuthAction}
import identifiers.register.partnership.MoreThanTenPartnersId
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

class MoreThanTenPartnersControllerSpec extends ControllerSpecBase {

  val view: moreThanTen = app.injector.instanceOf[moreThanTen]

  "MoreThanTenPartnersController" must {

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller.onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller.onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "check the view model is correct" in {
      implicit val request: DataRequest[AnyContent] =
        DataRequest(fakeRequest, "", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None), UserAnswers())

      val expected: MoreThanTenViewModel = MoreThanTenViewModel(
        title = "moreThanTenPartners.title",
        heading = "moreThanTenPartners.heading",
        hint = "moreThanTenPartners.hint",
        postCall = routes.MoreThanTenPartnersController.onSubmit(NormalMode),
        id = MoreThanTenPartnersId,
        None,
        errorKey = "moreThanTenPartners.error.required"
      )

      val actual = controller.viewModel(NormalMode)

      actual mustBe expected
    }
  }

  def controller: MoreThanTenPartnersController =
    new MoreThanTenPartnersController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      FakeNavigator,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dontGetAnyData,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )
}
