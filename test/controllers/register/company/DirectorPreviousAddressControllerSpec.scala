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

package controllers.register.company

import java.time.LocalDate

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.{CompanyDetailsId, DirectorDetailsId, DirectorNinoId, DirectorPreviousAddressId}
import models.register.company.{CompanyDetails, DirectorDetails, DirectorNino}
import models.{Address, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{CountryOptions, FakeNavigator, InputOption}
import views.html.register.company.directorPreviousAddress

class DirectorPreviousAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressFormProvider()
  val form = formProvider()
  val index = Index(0)
  val directorName = "test first name test middle name test last name"
  val countryOptions: Seq[InputOption] = Seq(InputOption("country:AU", "Australia"), InputOption("territory:KY", "Cayman Islands"))
  val companyName = "test company name"
  val address = Address("test address line 1", "test address line 2", None, None, None, "GB")

  val validData = Json.obj(
    CompanyDetailsId.toString -> CompanyDetails(companyName, None, None),
    "directors" -> Json.arr(
      Json.obj(
        DirectorDetailsId.toString ->
          DirectorDetails("test first name", Some("test middle name"), "test last name", LocalDate.now),
        DirectorPreviousAddressId.toString ->
          address

      ),
      Json.obj(
        DirectorDetailsId.toString ->
          DirectorDetails("test", Some("test"), "test", LocalDate.now)
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new DirectorPreviousAddressController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider, new CountryOptions(countryOptions))

  def viewAsString(form: Form[_] = form) = directorPreviousAddress(
    frontendAppConfig, form, NormalMode, index, directorName, countryOptions
  )(fakeRequest, messages).toString

  "DirectorPreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("test address line 1", "test address line 2", None, None, None, "GB")))
    }

    "redirect to the next page" when {
      "valid data is submitted with country as GB" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "test address line 1"), ("addressLine2", "test address line 2"),
          ("postCode.postCode", "NE1 1NE"),
          "country" -> "GB"
        )

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
      "valid data is submitted with country as non GB" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "test address line 1"), ("addressLine2", "test address line 2"),
          "country" -> "CA"
        )

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
