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

package controllers.register.administratorPartnership.partners

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.partnership.partners.{PartnerAddressYearsId, PartnerNameId}
import identifiers.register.{BusinessNameId, BusinessTypeId, RegistrationInfoId}
import models._
import models.register.BusinessType
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class PartnerAddressYearsControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new AddressYearsFormProvider()
  private val form = formProvider(Message("error.addressYears.required"))
  private val index = Index(0)
  private val partnerName = "test first name test last name"
  private val psaName = "Test Partnership Name"
  private val validData = Json.obj(
    BusinessNameId.toString -> "Test Partnership Name",
    BusinessTypeId.toString -> BusinessType.BusinessPartnership.toString,
    RegistrationInfoId.toString -> RegistrationInfo(
      RegistrationLegalStatus.Partnership, "", noIdentifier = false, RegistrationCustomerType.UK, None, None),
    "partners" -> Json.arr(
      Json.obj(
        PartnerNameId.toString ->
          PersonName("test first name", "test last name"),
        PartnerAddressYearsId.toString ->
          AddressYears.options.head.value.toString
      ),
      Json.obj(
        PartnerNameId.toString ->
          PersonName("test", "test")
      )
    )
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartnershipPartner) =
    new PartnerAddressYearsController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  val view: addressYears = app.injector.instanceOf[addressYears]

  private lazy val viewModel =
    AddressYearsViewModel(
      postCall = routes.PartnerAddressYearsController.onSubmit(NormalMode, index),
      title = Message("addressYears.heading", Message("thePartner")),
      heading = Message("addressYears.heading", partnerName),
      legend = Message("addressYears.heading", partnerName),
      psaName = Some(psaName),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )

  private def viewAsString(form: Form[?] = form) =
    view(form,
      viewModel,
      NormalMode
    )(fakeRequest, messages).toString

  "PartnersAddressYears Controller" must {

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
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}
