/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.individual

import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.individual.IndividualPreviousAddressId
import models.{Address, Index, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import utils._
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class IndividualPreviousAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()
  def countryOptions: CountryOptions = new CountryOptions(options)

  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))
  val messagePrefix = "common.previousAddress"
  val firstIndex = Index(0)

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val form: Form[Address] = formProvider()

  val viewmodel = ManualAddressViewModel(
    postCall = routes.IndividualPreviousAddressController.onSubmit(NormalMode),
    countryOptions = options,
    title = Message(s"$messagePrefix.title"),
    heading = Message(s"$messagePrefix.heading"),
    secondaryHeader = Some("common.individual.secondary.heading"),
    hint = Message(s"$messagePrefix.lede")
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new IndividualPreviousAddressController(frontendAppConfig, messagesApi,
      FakeDataCacheConnector, FakeNavigator)

  def viewAsString(form: Form[_] = form) = manualAddress(frontendAppConfig, )(fakeRequest, messages).toString

  "CompanyPreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(CompanyPreviousAddressId.toString -> Address("value 1", "value 2", None, None, None, "GB"))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "GB")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
