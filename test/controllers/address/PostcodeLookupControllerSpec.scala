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

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions.FakeAllowAccessProvider
import forms.address.PostCodeLookupFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, TolerantAddress, _}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
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
import uk.gov.hmrc.http.HttpException
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

object PostcodeLookupControllerSpec {

  object FakeIdentifier extends TypedIdentifier[Seq[TolerantAddress]]

  val postCall: Call = Call("POST", "www.example.com")
  val manualCall: Call = Call("GET", "www.example.com")

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val addressLookupConnector: AddressLookupConnector,
                                  override val navigator: Navigator,
                                  formProvider: PostCodeLookupFormProvider
                                ) extends PostcodeLookupController {

    override val allowAccess = FakeAllowAccessProvider()

    def onPageLoad(viewmodel: PostcodeLookupViewModel, answers: UserAnswers): Future[Result] =
      get(viewmodel, NormalMode)(DataRequest(FakeRequest(), "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))

    def onSubmit(viewmodel: PostcodeLookupViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(FakeIdentifier, viewmodel, NormalMode, invalidError, noResultError)(DataRequest(request,
        "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))

    private val invalidError: Message = "foo"

    private val noResultError: Message = "bar"

    override protected def form: Form[String] = formProvider()
  }

}

class PostcodeLookupControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  val viewmodel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com"),
    "test-title",
    "test-heading",
    "test-enter-postcode",
    Some("test-enter-postcode-link"),
    "test-form-label"
  )

  import PostcodeLookupControllerSpec._

  "get" must {
    "return a successful result" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val mat: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual postcodeLookup(appConfig, formProvider(), viewmodel, NormalMode)(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect on successful submission" in {

      val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      val address = TolerantAddress(Some(""), Some(""), None, None, None, Some("GB"))

      when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn Future.successful {
        Seq(address)
      }

      when(cacheConnector.save(eqTo("cacheId"), eqTo(FakeIdentifier), eqTo(Seq(address)))(any(), any(), any())) thenReturn Future.successful {
        Json.obj()
      }

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector)
      )) {
        app =>

          implicit val mat: Materializer = app.materializer

          val request = FakeRequest()
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request" when {
      "the postcode look fails to return result" in {

        val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn
          Future.failed(new HttpException("Failed", INTERNAL_SERVER_ERROR))

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersCacheConnector].toInstance(cacheConnector),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val request = FakeRequest()
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual postcodeLookup(appConfig, formProvider().withError("value", "foo"), viewmodel, NormalMode)(request, messages).toString
        }
      }
      "the postcode is invalid" in {

        val invalidPostcode = "*" * 10

        val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        verifyZeroInteractions(addressConnector)

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersCacheConnector].toInstance(cacheConnector),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val request = FakeRequest().withFormUrlEncodedBody("value" -> invalidPostcode)

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request)
            val form = formProvider().bind(Map("value" -> invalidPostcode))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual postcodeLookup(appConfig, form, viewmodel, NormalMode)(request, messages).toString
        }
      }
    }

    "return ok" when {
      "the postcode returns no results" which {
        "presents with form errors" in {

          val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
          val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

          when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn Future.successful {
            Seq.empty
          }

          running(_.overrides(
            bind[Navigator].toInstance(FakeNavigator),
            bind[UserAnswersCacheConnector].toInstance(cacheConnector),
            bind[AddressLookupConnector].toInstance(addressConnector)
          )) {
            app =>

              implicit val mat: Materializer = app.materializer

              val appConfig = app.injector.instanceOf[FrontendAppConfig]
              val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
              val request = FakeRequest()
              val messages = app.injector.instanceOf[MessagesApi].preferred(request)
              val controller = app.injector.instanceOf[TestController]
              val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

              status(result) mustEqual OK
              contentAsString(result) mustEqual postcodeLookup(appConfig, formProvider().withError("value", "bar"), viewmodel, NormalMode)(request, messages).toString
          }
        }
      }
    }
  }
}
