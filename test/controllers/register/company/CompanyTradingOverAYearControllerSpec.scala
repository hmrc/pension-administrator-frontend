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

package controllers.register.company

import connectors.cache.{FakeUserAnswersCacheConnector, FeatureToggleConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.company.routes._
import forms.HasReferenceNumberFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class CompanyTradingOverAYearControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("trading.error.required", companyName)

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = CompanyTradingOverAYearController.onSubmit(NormalMode),
      title = Message("trading.title", Message("theCompany")),
      heading = Message("trading.title", companyName),
      mode = NormalMode,
      hint = None,
      entityName = companyName
    )

  private def controller(dataRetrievalAction: DataRetrievalAction = getCompany,
                         featureToggleConnector: FeatureToggleConnector = FakeFeatureToggleConnector.disabled) =
    new CompanyTradingOverAYearController(frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view,
      featureToggleConnector
    )

  private def viewAsString(form: Form[_] = form, mode: Mode = NormalMode): String =
    view(form, viewModel)(fakeRequest, messages).toString

  "HasBeenTradingCompanyController" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}
