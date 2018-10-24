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

package controllers.address

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, RegistrationConnector, UserAnswersCacheConnector}
import forms.address.NonUKAddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.RegistrationInfoId
import models._
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.{ExecutionContext, Future}


class NonUKAddressControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  import NonUKAddressControllerSpec._

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

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual nonukAddress(appConfig, formProvider(), viewModel)(request, messages).toString

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

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers(Json.obj(fakeAddressId.toString -> testAddress.toTolerantAddress)))

            status(result) mustEqual OK
            contentAsString(result) mustEqual nonukAddress(appConfig, formProvider().fill(testAddress), viewModel)(request, messages).toString

        }
      }
    }
  }

  "post" must {

    "redirect to the postCall on valid data request" which {
      "will save address and registration info to answers" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

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
              "country" -> "IN")
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual onwardRoute.url

            val address = Address("value 1", "value 2", None, None, None, "IN")

            FakeUserAnswersCacheConnector.verify(fakeAddressId, address.toTolerantAddress)
            FakeUserAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
        }

      }

      "return BAD_REQUEST with view on invalid data request" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[NonUKAddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val form = formProvider().bind(Map.empty[String, String])

            val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual nonukAddress(appConfig, form, viewModel)(request, messages).toString
        }

      }

    }
  }

}


object NonUKAddressControllerSpec {

  val fakeAddressId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress] {
    override def toString = "fakeAddressId"
  }
  val externalId: String = "test-external-id"
  val companyName = "test company name"
  val sapNumber = "test-sap-number"
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)
  val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany,
    sapNumber,
    false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = ???

    override def registerWithNoIdOrganisation
    (name: String, address: Address)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] = Future.successful(registrationInfo)

    override def registerWithIdIndividual
    (nino: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = ???
  }

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val registrationConnector: RegistrationConnector,
                                  override val navigator: Navigator,
                                  formProvider: NonUKAddressFormProvider
                                ) extends NonUKAddressController {

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(fakeAddressId, viewModel)(DataRequest(FakeRequest(), "cacheId", psaUser, answers))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(companyName, fakeAddressId, viewModel)(DataRequest(request, externalId, psaUser, answers))

    override protected val form: Form[Address] = formProvider()
  }

}
