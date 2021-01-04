/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.FakeAllowAccessProvider
import forms.AddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.individual.{IndividualAddressChangedId, IndividualContactAddressId}
import models._
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.{ExecutionContext, Future}

object ManualAddressControllerSpec extends SpecBase {

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

  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None)
  val view: manualAddress = app.injector.instanceOf[manualAddress]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: AddressFormProvider,
                                  val view: manualAddress
                                )(implicit val executionContext: ExecutionContext) extends ManualAddressController {

    override val allowAccess = FakeAllowAccessProvider(config = frontendAppConfig)

    def onPageLoad(viewModel: ManualAddressViewModel, answers: UserAnswers): Future[Result] =
      get(viewModel, NormalMode)(DataRequest(FakeRequest(), "cacheId", psaUser, answers))

    def onSubmit(viewModel: ManualAddressViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest(),
                 mode:Mode = NormalMode, id: TypedIdentifier[Address] = fakeAddressId): Future[Result] =
      post(id, viewModel, mode)(
        DataRequest(request, externalId, psaUser, answers))

    override protected val form: Form[Address] = formProvider()

    override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
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
          bind[Navigator].to(FakeNavigator)
        )) {
          app =>

            val request = FakeRequest()

            val formProvider = app.injector.instanceOf[AddressFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]

            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(formProvider(), viewModel, NormalMode)(request, messages).toString

        }
      }
    }
  }

  "post in update mode" must {
    "redirect to the postCall on valid data request" which {
      "will save address to answers, save complete flag if not new and set the changed flag" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersCacheConnector].to(FakeUserAnswersCacheConnector),
          bind[Navigator].to(navigator)
        )) {
          app =>

            val controller = app.injector.instanceOf[TestController]

            val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest().withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("postCode", "AB1 1AB"),
              "country" -> "GB"),
              UpdateMode,
              IndividualContactAddressId
            )

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual onwardRoute.url

            val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

            FakeUserAnswersCacheConnector.verify(IndividualContactAddressId, address)
            FakeUserAnswersCacheConnector.verify(IndividualAddressChangedId, true)
        }
      }
    }
  }

  "post" must {

    "redirect to the postCall on valid data request" which {
      "will save address to answers" in {

        val onwardRoute = Call("GET", "/")

        val navigator = new FakeNavigator(onwardRoute, NormalMode)

        running(_.overrides(
          bind[CountryOptions].to[FakeCountryOptions],
          bind[UserAnswersCacheConnector].to(FakeUserAnswersCacheConnector),
          bind[Navigator].to(navigator)
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
        }
      }
    }

    "return BAD_REQUEST with view on invalid data request" in {

      running(_.overrides(
        bind[CountryOptions].to[FakeCountryOptions],
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val request = FakeRequest()

          val formProvider = app.injector.instanceOf[AddressFormProvider]
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]

          val form = formProvider().bind(Map.empty[String, String])

          val result = controller.onSubmit(viewModel, UserAnswers(), request.withFormUrlEncodedBody())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(form, viewModel, NormalMode)(request, messages).toString
      }

    }

  }

}
