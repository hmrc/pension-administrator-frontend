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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import forms.address.{ConfirmPreviousAddressFormProvider, SameContactAddressFormProvider}
import identifiers.TypedIdentifier
import identifiers.register.individual.IndividualSameContactAddressId
import models._
import models.requests.DataRequest
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.{ExecutionContext, Future}


class ConfirmPreviousAddressControllerSpec extends SpecBase {

  import ConfirmPreviousAddressControllerSpec._

  "get" must {

    "return a successful result when user has not answered the question previously" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[SameContactAddressFormProvider]
          val request = FakeRequest()
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(), UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider("error.required"), viewmodel(), countryOptions)(request, messages).toString
      }
    }

    "return a successful result when user has answered the question previously" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[SameContactAddressFormProvider]
          val request = FakeRequest()
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(true).asOpt.value
          val result = controller.onPageLoad(viewmodel(), answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider("error.required").fill(true),
            viewmodel(),
            countryOptions
          )(request, messages).toString
      }
    }
  }

  "post" should {

    s"return a redirect when the submitted data is valid and the data is changed with value true" in {

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          when(cacheConnector.save[Boolean, FakeIdentifier.type](
            any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, PreviousAddressId.type](
            any(), eqTo(PreviousAddressId), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )

          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers().set(FakeIdentifier)(false).asOpt.value, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    s"return a redirect when the submitted data is valid and the data is changed with value false" in {

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          when(cacheConnector.save[Boolean, FakeIdentifier.type](
            any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, PreviousAddressId.type](
            any(), eqTo(PreviousAddressId), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "false"
          )

          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers().set(FakeIdentifier)(false).asOpt.value, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }


    "return a redirect and save the data when the there is no existing data" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]
      val userAnswers = UserAnswers().set(IndividualSameContactAddressId)(true).asOpt.value.json

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(Some(userAnswers)))
      )) {
        app =>

          when(cacheConnector.save[Boolean, FakeIdentifier.type](
            any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, PreviousAddressId.type](
            any(), eqTo(PreviousAddressId), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          verify(cacheConnector, times(2)).save(any(), any(), any())(any(), any(), any())
      }
    }


    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[ConfirmPreviousAddressFormProvider]
          val request = FakeRequest()
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider(errorMessage(messages)).bind(Map.empty[String, String]),
            viewmodel(),
            countryOptions
          )(request, messages).toString
      }
    }
  }

}


object ConfirmPreviousAddressControllerSpec extends SpecBase with MockitoSugar {

  object FakeIdentifier extends TypedIdentifier[Boolean]

  object PreviousAddressId extends TypedIdentifier[Address]

  val view: sameContactAddress = app.injector.instanceOf[sameContactAddress]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  override val countryOptions: CountryOptions,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: sameContactAddress
                                )(implicit val executionContext: ExecutionContext) extends ConfirmPreviousAddressController {

    def onPageLoad(viewmodel: SameContactAddressViewModel, answers: UserAnswers): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)
      get(FakeIdentifier, viewmodel)
    }

    def onSubmit(viewmodel: SameContactAddressViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)
      post(FakeIdentifier, PreviousAddressId, viewmodel, NormalMode)
    }
  }

  private def testAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some("test post code"), Some("GB")
  )

  private def viewmodel() = SameContactAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("hint"),
    address = testAddress,
    psaName = "Test name",
    mode = NormalMode
  )

  private def errorMessage(implicit messages: Messages): String =
    messages("confirmPreviousAddress.error", "Test name")


}
