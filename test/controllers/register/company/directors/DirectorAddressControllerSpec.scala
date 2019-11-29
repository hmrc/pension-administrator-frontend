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

package controllers.register.company.directors

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.DirectorsOrPartnersChangedId
import identifiers.register.company.directors.{DirectorAddressId, DirectorNameId}
import models._
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val form: Form[Address] = formProvider()

  private val jonathanDoe = PersonName("Jonathan", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")

  private val doeResidence = Address("address line 1", "address line 2", Some("test town"), Some("test county"), Some("test post code"), "GB")
  private val bloggsResidence = Address("address line 1", "address line 2", Some("test town 2"), Some("test county 2"), Some("test post code 2"), "GB")

  private val directors = Json.obj(
    "directors" -> Json.arr(
      Json.obj(
        DirectorNameId.toString -> jonathanDoe
      ),
      Json.obj(
        DirectorNameId.toString -> joeBloggs
      )
    )
  )

  private val data = new FakeDataRetrievalAction(Some(directors))

  private val directorsWithAddresses = Json.obj(
    "directors" -> Json.arr(
      Json.obj(
        DirectorNameId.toString -> jonathanDoe,
        DirectorAddressId.toString -> doeResidence
      ),
      Json.obj(
        DirectorNameId.toString -> joeBloggs,
        DirectorAddressId.toString -> bloggsResidence
      )
    )
  )

  private val dataWithAddresses = new FakeDataRetrievalAction(Some(directorsWithAddresses))

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private val auditService = new StubSuccessfulAuditService()
  val view: manualAddress = app.injector.instanceOf[manualAddress]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new DirectorAddressController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAllowAccessProvider(),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      auditService,
      stubMessagesControllerComponents(),
      view
    )

  private val viewModel = ManualAddressViewModel(
    routes.DirectorAddressController.onSubmit(NormalMode, firstIndex),
    countryOptions.options,
    Message("contactAddress.heading", Message("theDirector").resolve),
    Message("contactAddress.heading", jonathanDoe.fullName)
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

    "send an audit event when valid data is submitted" in {

      val existingAddress = Address(
        "existing-line-1",
        "existing-line-2",
        None,
        None,
        None,
        "existing-country"
      )

      val selectedAddress = TolerantAddress(None, None, None, None, None, None)

      val data =
        UserAnswers()
          .directorAddress(firstIndex, existingAddress)
          .companyDirectorAddressList(firstIndex, selectedAddress)
          .dataRetrievalAction

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      auditService.reset()

      val result = controller(data).onSubmit(NormalMode, firstIndex)(postRequest)

      whenReady(result) {
        _ =>
          auditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.LookupChanged,
              s"Company Director Address: ${jonathanDoe.fullName}",
              Address(
                "value 1",
                "value 2",
                None,
                None,
                Some("NE1 1NE"),
                "GB"
              )
            )
          )
      }

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
