/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.{BusinessNameId, DirectorsOrPartnersChangedId, RegistrationInfoId}
import identifiers.register.company.directors.DirectorNameId
import models.FeatureToggle.Enabled
import models.FeatureToggleName.PsaRegistration
import models.RegistrationCustomerType.UK
import models.RegistrationLegalStatus.LimitedCompany
import models._
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val form: Form[Address] = formProvider()

  private val jonathanDoe = PersonName("Jonathan", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")

  val registrationInfo = RegistrationInfo(LimitedCompany, "", noIdentifier=false, UK, Some(RegistrationIdType.Nino), Some("AB121212C"))

  private val directors = Json.obj(
    "directors" -> Json.arr(
      Json.obj(
        DirectorNameId.toString -> jonathanDoe
      ),
      Json.obj(
        DirectorNameId.toString -> joeBloggs
      )
    ),
    RegistrationInfoId.toString -> registrationInfo,
    BusinessNameId.toString -> companyName
  )

  private val data = new FakeDataRetrievalAction(Some(directors))

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val view: manualAddress = app.injector.instanceOf[manualAddress]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new DirectorAddressController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAllowAccessProvider(config = frontendAppConfig),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      controllerComponents,
      view,
      FakeFeatureToggleConnector.returns(Enabled(PsaRegistration))
    )

  private val viewModel = ManualAddressViewModel(
    routes.DirectorAddressController.onSubmit(NormalMode, firstIndex),
    countryOptions.options,
    Message("enter.address.heading", Message("theDirector")),
    Message("enter.address.heading", jonathanDoe.fullName),
    psaName = Some(companyName),
    returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
  )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      viewModel,
      NormalMode
    )(fakeRequest, messages).toString

  "directorAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(data).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      val result = controller(data).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(data).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

    "redirect to the next page when valid data is submitted and update the change flag when in update mode" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      val result = controller(data).onSubmit(UpdateMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, true)
    }
  }

}
