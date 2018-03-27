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

package controllers.register.company.directors

import java.time.LocalDate

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.directors.{DirectorAddressId, DirectorDetailsId}
import models.register.company.directors.DirectorDetails
import models.{Address, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, InputOption}
import views.html.register.company.directors.directorAddress

class DirectorAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val form: Form[Address] = formProvider()

  val firstIndex = Index(0)

  val jonathanDoe = DirectorDetails("Jonathan", None, "Doe", LocalDate.now())
  val joeBloggs = DirectorDetails("Joe", None, "Bloggs", LocalDate.now())

  val doeResidence = Address("address line 1", "address line 2", Some("test town"), Some("test county"), Some("test post code"), "GB")
  val bloggsResidence = Address("address line 1", "address line 2", Some("test town 2"), Some("test county 2"), Some("test post code 2"), "GB")

  val directors = Json.obj(
    "directors" -> Json.arr(
      Json.obj(
        DirectorDetailsId.toString -> jonathanDoe
      ),
      Json.obj(
        DirectorDetailsId.toString -> joeBloggs
      )
    )
  )

  val data = new FakeDataRetrievalAction(Some(directors))

  val directorsWithAddresses = Json.obj(
    "directors" -> Json.arr(
      Json.obj(
        DirectorDetailsId.toString -> jonathanDoe,
        DirectorAddressId.toString -> doeResidence
      ),
      Json.obj(
        DirectorDetailsId.toString -> joeBloggs,
        DirectorAddressId.toString -> bloggsResidence
      )
    )
  )

  val dataWithAddresses = new FakeDataRetrievalAction(Some(directorsWithAddresses))

  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  def countryOptions: CountryOptions = new CountryOptions(options)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new DirectorAddressController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions
    )

  def viewAsString(form: Form[_] = form) = directorAddress(
    frontendAppConfig,
    form,
    NormalMode,
    firstIndex,
    jonathanDoe.fullName,
    options
  )(fakeRequest, messages).toString

  "directorAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(data).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val result = controller(dataWithAddresses).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(doeResidence))
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

  }
}
