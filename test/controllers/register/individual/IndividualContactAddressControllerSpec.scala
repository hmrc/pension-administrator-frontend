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

package controllers.register.individual

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.*
import forms.{AddressFormProvider, UKOnlyAddressFormProvider}
import identifiers.register.AreYouInUKId
import models.admin.ukResidencyToggle
import models.{Address, AddressUKOnly, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.libs.json.JsResult
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.countryOptions.CountryOptions
import utils.navigators.IndividualNavigatorV2
import utils.{FakeCountryOptions, FakeNavigator, FeatureFlagMockHelper, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class IndividualContactAddressControllerSpec
  extends ControllerSpecBase
    with ScalaFutures
    with OptionValues
    with FeatureFlagMockHelper
    with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val messagePrefix = "individual.enter.address"

  val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  val form: Form[Address] = formProvider()
  val ukFormProvider = new UKOnlyAddressFormProvider()
  val formUK: Form[AddressUKOnly] = ukFormProvider()

  val navigatorV2: IndividualNavigatorV2 = mock[IndividualNavigatorV2]

  val viewmodel: ManualAddressViewModel = ManualAddressViewModel(
    postCall = routes.IndividualContactAddressController.onSubmit(NormalMode),
    countryOptions = countryOptions.options,
    title = Message(s"$messagePrefix.heading"),
    heading = Message(s"$messagePrefix.heading")
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new IndividualContactAddressController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      navigatorV2,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      ukFormProvider,
      mockFeatureFlagService,
      countryOptions,
      controllerComponents,
      view
    )
  private val isUkHintText = false
  val view: manualAddress = app.injector.instanceOf[manualAddress]

  val validData: JsResult[UserAnswers] = UserAnswers()
    .set(AreYouInUKId)(true)
  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  def viewAsString(form: Form[?] = form): String = view(form, viewmodel, NormalMode, isUkHintText)(fakeRequest, messages).toString
  def viewAsStringToggleEnabled(form: Form[?] = formUK): String = view(form, viewmodel, NormalMode, isUkHintText)(fakeRequest, messages).toString

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    featureFlagMock(ukResidencyToggle)
  }
  
  "IndividualContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view for a GET when toggle is enabled" in {
      featureFlagMock(ukResidencyToggle, isEnabled = true)
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsStringToggleEnabled()
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

    "redirect to the next page when valid data is submitted with toggle enabled" in {
      featureFlagMock(ukResidencyToggle, isEnabled = true)
      when(navigatorV2.nextPage(any(), any(), any()))
        .thenReturn(onwardRoute)
      
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE")
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

      "return a Bad Request and errors when invalid data is submitted with toggle enabled" in {
        featureFlagMock(ukResidencyToggle, isEnabled = true)
        
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formUK.bind(Map("value" -> "invalid value"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsStringToggleEnabled(boundForm)
      }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

  }
}
