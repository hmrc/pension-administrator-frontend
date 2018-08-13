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
import forms.address.AddressYearsFormProvider
import identifiers.register.company.CompanyDetailsId
import identifiers.register.company.directors.{DirectorAddressYearsId, DirectorDetailsId}
import models.register.company.CompanyDetails
import models.{AddressYears, Index, NormalMode, PersonDetails}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class DirectorAddressYearsControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressYearsFormProvider()
  private val form = formProvider(Message("error.addressYears.required"))
  private val index = Index(0)
  private val directorName = "test first name test middle name test last name"

  private val validData = Json.obj(
    CompanyDetailsId.toString -> CompanyDetails(None, None),
    "directors" -> Json.arr(
      Json.obj(
        DirectorDetailsId.toString ->
          PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now),
        DirectorAddressYearsId.toString ->
          AddressYears.options.head.value.toString
      ),
      Json.obj(
        DirectorDetailsId.toString ->
          PersonDetails("test", Some("test"), "test", LocalDate.now)
      )
    )
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new DirectorAddressYearsController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private lazy val viewModel =
    AddressYearsViewModel(
      postCall = routes.DirectorAddressYearsController.onSubmit(NormalMode, index),
      title = Message("directorAddressYears.title"),
      heading = Message("directorAddressYears.heading"),
      legend = Message("directorAddressYears.heading"),
      Some(Message(directorName))
    )

  private def viewAsString(form: Form[_] = form) =
    addressYears(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages).toString

  "DirectorAddressYears Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to the next page when valid data is submitted" in {

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
