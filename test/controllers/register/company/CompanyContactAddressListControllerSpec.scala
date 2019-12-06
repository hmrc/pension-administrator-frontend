/*
 * Copyright 2019 HM Revenue & Customs
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

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.PhoneFormProvider
import forms.address.AddressListFormProvider
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class CompanyContactAddressListControllerSpec extends ControllerSpecBase {
  def onwardRoute: Call = routes.CompanyContactAddressController.onPageLoad(NormalMode)

  def application(data: DataRetrievalAction): Application =
    applicationBuilder(data).build()

  lazy val view: addressList = inject[addressList]

  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  private val dataRetrievalAction =
    UserAnswers().businessName().companyContactAddressList(addresses).dataRetrievalAction

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.CompanyContactAddressListController.onSubmit(NormalMode),
      routes.CompanyContactAddressController.onPageLoad(NormalMode),
      addresses,
      Message("select.address.heading", Message("theCompany")),
      Message("select.address.heading", "test company"),
      Message("select.address.hint.text"),
      Message("manual.entry.link")
    )
  }

  "company Contact Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      running(_.overrides(modules(dataRetrievalAction): _*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val result = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) mustBe OK
          val viewModel: AddressListViewModel = addressListViewModel(addresses)
          val form = new AddressListFormProvider()(viewModel.addresses, "error.required")

          contentAsString(result) mustBe view(form, viewModel, NormalMode)(fakeRequest, messagesApi.preferred(fakeRequest)).toString
      }
    }

    "redirect to Company Address Post Code Lookup if no address data on a GET request" in {
      running(_.overrides(modules(getEmptyData): _*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val result = controller.onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {
      running(_.overrides(modules(dontGetAnyData): _*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val result = controller.onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to the next page on POST of valid data" in {
      running(_.overrides(modules(dataRetrievalAction)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      running(_.overrides(modules(dontGetAnyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to Company Address Post Code Lookup if no address data on a POST request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)
      }
    }
  }
}
