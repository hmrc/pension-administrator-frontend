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
import identifiers.register.partnership.PartnershipPreviousAddressPostCodeLookupId
import models.{NormalMode, TolerantAddress}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class PartnershipPreviousAddressListControllerSpec extends ControllerSpecBase {

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

  val view: addressList = app.injector.instanceOf[addressList]

  private val data =
    UserAnswers(Json.obj())
    .businessName("Test Partnership Name")
      .set(PartnershipPreviousAddressPostCodeLookupId)(addresses)
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  "partnership Previous Address List Controller" must {

    "return Ok and the correct view on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction)
      )) { implicit app =>
        val request = addCSRFToken(FakeRequest(routes.PartnershipPreviousAddressListController.onPageLoad(NormalMode)))
        val result = route(app, request).value

        status(result) mustBe OK

        def viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form = new AddressListFormProvider()(viewModel.addresses, "error.required")

        contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messagesApi.preferred(fakeRequest)).toString
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(getPartnership)
      )) { implicit app =>
        val request = FakeRequest(routes.PartnershipPreviousAddressListController.onPageLoad(NormalMode))
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(NormalMode).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(dontGetAnyData)
      )) { implicit app =>
        val request = FakeRequest(routes.PartnershipPreviousAddressListController.onPageLoad(NormalMode))
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

    }

    "redirect to the next page on POST of valid data" in {
      val onwardRoute = controllers.register.administratorPartnership.routes.PartnershipEmailController.onPageLoad(NormalMode)
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction),
        bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).toInstance(new FakeNavigator(desiredRoute = onwardRoute))
      )) { implicit app =>
        val controller = app.injector.instanceOf[PartnershipPreviousAddressListController]
        val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
        val result = controller.onSubmit(NormalMode)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(dontGetAnyData)
      )) { implicit app =>
        val controller = app.injector.instanceOf[PartnershipPreviousAddressListController]
        val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
        val result = controller.onSubmit(NormalMode)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a POST request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[DataRetrievalAction].toInstance(getPartnership)
      )) { implicit app =>

        val controller = app.injector.instanceOf[PartnershipPreviousAddressListController]
        val request = FakeRequest().withFormUrlEncodedBody("value" -> "0")
        val result = controller.onSubmit(NormalMode)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(NormalMode).url)
      }

    }
  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.PartnershipPreviousAddressListController.onSubmit(NormalMode),
      routes.PartnershipPreviousAddressController.onPageLoad(NormalMode),
      addresses,
      Message("select.previous.address.heading", Message("thePartnership")),
      Message("select.previous.address.heading", "Test Partnership Name"),
      Message("select.address.hint.text"),
      Message("manual.entry.link")
    )
  }

}
