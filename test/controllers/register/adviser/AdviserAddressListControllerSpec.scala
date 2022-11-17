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

package controllers.register.adviser

import connectors.cache.{FakeUserAnswersCacheConnector, FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.adviser.{AdviserAddressPostCodeLookupId, AdviserNameId}
import models.FeatureToggle.Enabled
import models.FeatureToggleName.PsaRegistration
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class AdviserAddressListControllerSpec extends ControllerSpecBase {

  private val onwardRoute = routes.AdviserAddressController.onPageLoad(NormalMode)
  val name = "Adviser name"
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

  private val data =
    UserAnswers(Json.obj())
      .set(AdviserNameId)(name)
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(addresses))
      .asOpt.map(_.json)

  val view: addressList = app.injector.instanceOf[addressList]

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  "Adviser Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      val viewModel: AddressListViewModel = addressListViewModel(addresses)
      val form = new AddressListFormProvider()(viewModel.addresses, "error.required")

      val app = application(dataRetrievalAction)

      val request = addCSRFToken(FakeRequest(GET, routes.AdviserAddressListController.onPageLoad(NormalMode).url))

      val result = route(app, request).value

      status(result) mustBe OK

      contentAsString(result) mustBe view(form, addressListViewModel(addresses), NormalMode)(request, messages).toString()


    }

    "redirect to Adviser Address Post Code Lookup if no address data on a GET request" in {
      val app = application(getEmptyData)

      val request = FakeRequest(GET, routes.AdviserAddressListController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)


    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {
      val app = application(dontGetAnyData)

      val request = FakeRequest(GET, routes.AdviserAddressListController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)


    }

    "redirect to the next page on POST of valid data" in {
      running(_.overrides(modules(dataRetrievalAction)++
        Seq[GuiceableModule](
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[FeatureToggleConnector].toInstance(FakeFeatureToggleConnector.returns(Enabled(PsaRegistration)))):_*)) {
        app =>
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(routes.AdviserEmailController.onPageLoad(NormalMode).url)

      }
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      running(_.overrides(modules(dontGetAnyData)++
        Seq[GuiceableModule](
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[FeatureToggleConnector].toInstance(FakeFeatureToggleConnector.returns(Enabled(PsaRegistration)))):_*)) {
        app =>
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }

    }

    "redirect to Adviser Address Post Code Lookup if no address data on a POST request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[FeatureToggleConnector].toInstance(FakeFeatureToggleConnector.returns(Enabled(PsaRegistration)))):_*)) {
        app =>
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)

      }
    }
  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.AdviserAddressListController.onSubmit(NormalMode),
      routes.AdviserAddressController.onPageLoad(NormalMode),
      addresses,
      Message("select.address.heading", Message("theAdviser")),
      Message("select.address.heading", name),
      Message("select.address.hint.text"),
      Message("manual.entry.link")
    )
  }

  def application(dataRetrievalAction: DataRetrievalAction): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrievalAction),
      bind[MessagesControllerComponents].to(controllerComponents),
      bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind[FeatureToggleConnector].toInstance(FakeFeatureToggleConnector.returns(Enabled(PsaRegistration)))
    ).build()
}
