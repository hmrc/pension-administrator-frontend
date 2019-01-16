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

package controllers.address

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import forms.AddressFormProvider
import identifiers.TypedIdentifier
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
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

object ManualAddressControllerSpec {

  val fakeAddressId: TypedIdentifier[Address] = new TypedIdentifier[Address] {
    override def toString = "fakeAddressId"
  }

  val fakeAddressListId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress] {
    override def toString = "fakeAddressListId"
  }

  val externalId: String = "test-external-id"

  val fakeSeqTolerantAddressId: TypedIdentifier[Seq[TolerantAddress]] = new TypedIdentifier[Seq[TolerantAddress]] {
    override def toString = "abc"
  }

  private val psaUser = PSAUser(UserType.Individual, "userId", None, isExistingPSA = false, None)
  private val testContext = "test-context"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: AddressFormProvider,
                                  override val auditService: AuditService
                                ) extends ManualAddressController {

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(fakeAddressId, fakeAddressListId, viewModel)(DataRequest(FakeRequest(), "cacheId", psaUser, answers))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(fakeAddressId, fakeAddressListId, viewModel, NormalMode, testContext, fakeSeqTolerantAddressId)(
        DataRequest(request, externalId, psaUser, answers))

    override protected val form: Form[Address] = formProvider()
  }

}

class ManualAddressControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  import ManualAddressControllerSpec._

  val addressData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "postCode" -> "AB1 1AP",
    "country" -> "GB"
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
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual manualAddress(appConfig, formProvider(), viewModel)(request, messages).toString

        }

      }

      "data is retrieved" in {
        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val testAddress = Address(
              "address line 1",
              "address line 2",
              Some("test town"),
              Some("test county"),
              Some("test post code"), "GB"
            )

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers(Json.obj(fakeAddressId.toString -> testAddress)))

            status(result) mustEqual OK
            contentAsString(result) mustEqual manualAddress(appConfig, formProvider().fill(testAddress), viewModel)(request, messages).toString

        }
      }

      "data is not retrieved but there is a selected address" in {

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[Navigator].to(FakeNavigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val testAddress = TolerantAddress(
              Some("address line 1"),
              Some("address line 2"),
              None,
              None,
              Some("test post code"),
              Some("GB")
            )

            val userAnswers = UserAnswers().set(fakeAddressListId)(testAddress).asOpt.value

            val request = FakeRequest()

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val form = formProvider().fill(testAddress.toAddress)

            val result = controller.onPageLoad(viewModel, userAnswers)

            status(result) mustEqual OK
            contentAsString(result) mustEqual manualAddress(appConfig, form, viewModel)(request, messages).toString

        }

      }

    }
  }

  "post" must {

    "redirect to the postCall on valid data request" which {
      "will save address to answers and remove the address postcode lookup list" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersCacheConnector].to(FakeUserAnswersCacheConnector),
          bind[Navigator].to(navigator),
          bind[AuditService].to[StubSuccessfulAuditService]
        )) {
          app =>

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB")
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual onwardRoute.url

            val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

            FakeUserAnswersCacheConnector.verify(fakeAddressId, address)
            FakeUserAnswersCacheConnector.verifyRemoved(fakeSeqTolerantAddressId)
        }

      }

      "will send an audit event" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)
        val auditService = new StubSuccessfulAuditService()

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersCacheConnector].to(FakeUserAnswersCacheConnector),
          bind[Navigator].to(navigator),
          bind[AuditService].toInstance(auditService)
        )) {
          app =>
            val existingAddress = Address(
              "existing-line-1",
              "existing-line-2",
              None,
              None,
              None,
              "existing-country"
            )

            val selectedAddress = TolerantAddress(None, None, None, None, None, None)

            val userAnswers =
              UserAnswers()
                .set(fakeAddressId)(existingAddress).asOpt.value
                .set(fakeAddressListId)(selectedAddress).asOpt.value

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, userAnswers, FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB")
            )

            whenReady(result) {
              _ =>
                auditService.verifySent(
                  AddressEvent(
                    externalId,
                    AddressAction.LookupChanged,
                    testContext,
                    Address(
                      "value 1",
                      "value 2",
                      None,
                      None,
                      Some("AB1 1AB"),
                      "GB"
                    )
                  )
                )
            }

        }

      }
    }

    "return BAD_REQUEST with view on invalid data request" in {

      running(_.overrides(
        bind[CountryOptions].to[FakeCountryOptions],
        bind[AuditService].to[StubSuccessfulAuditService],
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val request = FakeRequest()

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressFormProvider]
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]

          val form = formProvider().bind(Map.empty[String, String])

          val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual manualAddress(appConfig, form, viewModel)(request, messages).toString
      }

    }

  }

}
