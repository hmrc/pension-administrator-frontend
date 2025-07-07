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

package controllers.address

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.RegistrationConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.address.NonUKAddressFormProvider
import identifiers.register.RegistrationInfoId
import models.*
import models.requests.DataRequest
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.*
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.*
import utils.countryOptions.CountryOptions
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.{ExecutionContext, Future}


class NonUKAddressControllerSpec extends AnyWordSpecLike with MockitoSugar with ScalaFutures with OptionValues {

  import NonUKAddressControllerSpec.*

  val addressData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "country" -> "IN"
  )

  private val countryOptions = FakeCountryOptions.fakeCountries

  private val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions,
    "title",
    "heading",
    Some("secondary.header")
  )

  "get" must {
    "return OK with view" when {
      "data is not retrieved" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator)
        )) {
          app =>

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(formProvider(), viewModel)(request, messages).toString

        }

      }

      "data is retrieved" in {
        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator)
        )) {
          app =>

            val testAddress = Address(
              "address line 1",
              "address line 2",
              Some("test town"),
              Some("test county"),
              None,
              "IN"
            )

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers(Json.obj(fakeAddressId.toString -> testAddress.toTolerantAddress)))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(formProvider().fill(testAddress), viewModel)(request, messages).toString

        }
      }
    }
  }

  def controllerPostWithCountry(country:String):Unit = {
    s"redirect to the postCall on valid data request for country $country" which {
      "will save address and registration info to answers" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute)

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersCacheConnector].to(FakeUserAnswersCacheConnector),
          bind[Navigator].to(navigator),
          bind[RegistrationConnector].to(fakeRegistrationConnector)
        )) {
          app =>

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              "country" -> country)
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual onwardRoute.url

            val address = Address("value 1", "value 2", None, None, None, country)

            FakeUserAnswersCacheConnector.verify(fakeAddressId, address.toTolerantAddress)
            FakeUserAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
        }

      }
    }
  }

  "post" must {

      behave like controllerPostWithCountry("ES")

      behave like controllerPostWithCountry("GB")

      "return BAD_REQUEST with view on invalid data request" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val form = formProvider().bind(Map.empty[String, String])

            val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(form, viewModel)(request, messages).toString
        }

      }

    }


}


object NonUKAddressControllerSpec extends NonUKAddressControllerDataMocks {

  val view: nonukAddress = app.injector.instanceOf[nonukAddress]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val registrationConnector: RegistrationConnector,
                                  override val navigator: Navigator,
                                  override val countryOptions: CountryOptions,
                                  formProvider: NonUKAddressFormProvider,
                                  val view: nonukAddress
                                )(implicit val executionContext: ExecutionContext) extends NonUKAddressController {

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(fakeAddressId, viewModel)(DataRequest(FakeRequest(), "cacheId", psaUser, answers))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(testCompanyName, fakeAddressId, viewModel, RegistrationLegalStatus.LimitedCompany)(DataRequest(request, externalId, psaUser, answers))

    override protected val form: Form[Address] = formProvider()

    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents
  }

}
