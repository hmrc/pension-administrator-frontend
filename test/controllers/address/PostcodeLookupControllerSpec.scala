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
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.FakeAllowAccessProvider
import forms.address.PostCodeLookupFormProvider
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
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

import scala.concurrent.{ExecutionContext, Future}

object PostcodeLookupControllerSpec extends SpecBase {

  object FakeIdentifier extends TypedIdentifier[Seq[TolerantAddress]]

  val postCall: Call = Call("POST", "www.example.com")
  val manualCall: Call = Call("GET", "www.example.com")

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  class TestController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val addressLookupConnector: AddressLookupConnector,
                                  override val navigator: Navigator,
                                  formProvider: PostCodeLookupFormProvider,
                                  val view: postcodeLookup
                                )(implicit val executionContext: ExecutionContext) extends PostcodeLookupController {

    override val allowAccess = FakeAllowAccessProvider(config = frontendAppConfig)

    def onPageLoad(viewmodel: PostcodeLookupViewModel, answers: UserAnswers): Future[Result] =
      get(viewmodel, NormalMode)(DataRequest(FakeRequest(), "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers))

    def onSubmit(viewmodel: PostcodeLookupViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(FakeIdentifier, viewmodel, NormalMode, invalidError)(DataRequest(request,
        "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers))

    private val invalidError: Message = "foo"

    override protected def form: Form[String] = formProvider()

    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents
  }

}

class PostcodeLookupControllerSpec extends AnyWordSpecLike with Matchers with MockitoSugar with ScalaFutures with OptionValues {

  val viewmodel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com"),
    "test-title",
    "test-heading",
    "test-enter-postcode",
    Some("test-enter-postcode-link")
  )

  import PostcodeLookupControllerSpec._

  "get" must {
    "return a successful result" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(), viewmodel, NormalMode)(request, messages).toString
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

      when(cacheConnector.save(eqTo(FakeIdentifier), eqTo(Seq(address)))(any(), any(), any())) thenReturn Future.successful {
        Json.obj()
      }

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[AddressLookupConnector].toInstance(addressConnector)
      )) {
        app =>

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

            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val request = FakeRequest()
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(formProvider().withError("value", "foo"), viewmodel, NormalMode)(request, messages).toString
        }
      }
      "the postcode is invalid" in {

        val invalidPostcode = "*" * 10

        val cacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        verifyNoMoreInteractions(addressConnector)

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersCacheConnector].toInstance(cacheConnector),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            val request = FakeRequest().withFormUrlEncodedBody("value" -> invalidPostcode)

            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request)
            val form = formProvider().bind(Map("value" -> invalidPostcode))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(form, viewmodel, NormalMode)(request, messages).toString
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

              val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
              val request = FakeRequest()
              val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
              implicit val messages: Messages = messagesApi.preferred(request)
              val controller = app.injector.instanceOf[TestController]
              val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(
                formProvider().withError("value", Message("error.postcode.noResults", "ZZ1 1ZZ")),
                viewmodel, NormalMode)(request, messages).toString
          }
        }
      }
    }
  }
}
