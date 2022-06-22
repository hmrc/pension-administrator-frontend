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

package controllers.register.administratorPartnership

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipContactAddressPostCodeLookupId
import models.{NormalMode, TolerantAddress}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class PartnershipContactAddressListControllerSpec extends ControllerSpecBase with MustMatchers {

  val view: addressList = inject[addressList]

  val testName = "Test Partnership Name"

  val addresses: Seq[TolerantAddress] = Seq(
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

  val form = new AddressListFormProvider()(addresses, "error.required")

  val viewModel: AddressListViewModel = AddressListViewModel(
    routes.PartnershipContactAddressListController.onSubmit(NormalMode),
    routes.PartnershipContactAddressController.onPageLoad(NormalMode),
    addresses,
    Message("select.address.heading", Message("thePartnership")),
    Message("select.address.heading", testName)
  )

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    BusinessNameId.toString -> testName,
    PartnershipContactAddressPostCodeLookupId.toString -> addresses
  )))

  "PartnershipAddressListController" must {

    "render the view correctly on a GET request" in {
      val request = addCSRFToken(FakeRequest(GET, routes.PartnershipContactAddressListController.onPageLoad(NormalMode).url))
      val result = route(application, request).value
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messagesApi.preferred(request)).toString()

    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(retrieval) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)): _*)) {
        app =>
          val controller = app.injector.instanceOf[PartnershipContactAddressListController]
          val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
      }
    }
  }

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

}
