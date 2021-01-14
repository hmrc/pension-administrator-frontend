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

package controllers.register

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.AreYouInUKFormProvider
import identifiers.register.AreYouInUKId
import models.{CheckMode, Mode, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._

import utils.FakeNavigator
import viewmodels.{AreYouInUKViewModel, Message}
import views.html.register.areYouInUK

class BusinessTypeAreYouInUKControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AreYouInUKFormProvider()
  private val form = formProvider()

  val view: areYouInUK = app.injector.instanceOf[areYouInUK]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new BusinessTypeAreYouInUKController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAllowAccessProvider(config = frontendAppConfig),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewmodel(mode: Mode) =
    AreYouInUKViewModel(mode,
      postCall = routes.BusinessTypeAreYouInUKController.onSubmit(mode),
      title = Message("areYouInUK.title"),
      heading = Message("areYouInUK.heading"),
      p1 = Some("areYouInUK.check.selectedUkAddress"),
      p2 = Some("areYouInUK.check.provideNonUkAddress")
    )

  private def viewAsString(form: Form[_] = form, mode: Mode = NormalMode) = view(form, viewmodel(mode))(fakeRequest, messages).toString

  "Are You In the  UK Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(CheckMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(mode = CheckMode)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(AreYouInUKId.toString -> true)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest =
        fakeRequest
          .withFormUrlEncodedBody(
            ("value", "true")
          )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "xxx"))
      val boundForm = form.bind(Map("value" -> "xxx"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
