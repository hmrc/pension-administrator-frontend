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

package controllers.register.individual

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.individual.routes._
import forms.address.AddressListFormProvider
import identifiers.register.individual.IndividualContactAddressPostCodeLookupId
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Individual
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList
import play.api.test.CSRFTokenHelper.addCSRFToken

class IndividualContactAddressListControllerSpec extends ControllerSpecBase {

  val onwardRoute: Call = IndividualContactAddressController.onPageLoad(NormalMode)

  val view: addressList = app.injector.instanceOf[addressList]

  def application(dataRetrievalAction: DataRetrievalAction): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind[DataRetrievalAction].toInstance(dataRetrievalAction),
      bind(classOf[Navigator]).qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(desiredRoute = onwardRoute))
    ).build()

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
      .set(IndividualContactAddressPostCodeLookupId)(addresses)
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  "individual Contact Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      val app = application(dataRetrievalAction)

      val request = addCSRFToken(FakeRequest(GET, routes.IndividualContactAddressListController.onPageLoad(NormalMode).url))

      val result = route(app, request).value

      status(result) mustBe OK

        val viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form = new AddressListFormProvider()(viewModel.addresses, "error.required")

      contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messagesApi.preferred(request)).toString


    }

    "redirect to Individual Address Post Code Lookup if no address data on a GET request" in {
      val app = application(getEmptyData)

      val request = FakeRequest(GET, routes.IndividualContactAddressListController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)


    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {
      val app = application(dontGetAnyData)

      val request = FakeRequest(GET, routes.IndividualContactAddressListController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)


    }

    "redirect to the next page on POST of valid data" in {
      running(_.overrides(modules(dataRetrievalAction)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)):_*)) {
        app =>
          val controller = app.injector.instanceOf[IndividualContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(onwardRoute.url)

      }
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      running(_.overrides(modules(dontGetAnyData):_*)) {
        app =>
          val controller = app.injector.instanceOf[IndividualContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)

      }
    }

    "redirect to Individual Address Post Code Lookup if no address data on a POST request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)):_*)) {
        app =>
          val controller = app.injector.instanceOf[IndividualContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)
          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)
      }

    }
  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.IndividualContactAddressListController.onSubmit(NormalMode),
      routes.IndividualContactAddressController.onPageLoad(NormalMode),
      addresses,
      Message("individual.select.address.heading"),
      Message("individual.select.address.heading"),
      Message("select.address.hint.text"),
      Message("manual.entry.link"),
      selectAddressPostLink = Some(Message("individual.selectAddressPostLink.text"))
    )
  }
}
