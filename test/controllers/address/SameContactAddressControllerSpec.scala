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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import forms.address.SameContactAddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.individual.IndividualSameContactAddressId
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Form
import play.api.i18n.MessagesApi
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

object SameContactAddressControllerSpec extends SpecBase {

  object FakeIdentifier extends TypedIdentifier[Boolean]

  object RegAddressIdentifier extends TypedIdentifier[TolerantAddress]

  object ContactAddressIdentifier extends TypedIdentifier[Address]

  val view: sameContactAddress = app.injector.instanceOf[sameContactAddress]

  class TestController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: SameContactAddressFormProvider,
                                  override val countryOptions: CountryOptions,
                                  val view: sameContactAddress
                                )(implicit val executionContext: ExecutionContext) extends SameContactAddressController {

    def onPageLoad(viewmodel: SameContactAddressViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, viewmodel, form)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers))
    }

    def onSubmit(viewmodel: SameContactAddressViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, RegAddressIdentifier, ContactAddressIdentifier, viewmodel, NormalMode, form)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers))
    }

    val form: Form[Boolean] = formProvider("error.required")
    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents
  }

}

class SameContactAddressControllerSpec extends AnyWordSpecLike with Matchers with OptionValues with ScalaFutures with MockitoSugar {

  import SameContactAddressControllerSpec._

  def testAddress(line2: Option[String]) = TolerantAddress(
    Some("address line 1"),
    line2,
    Some("test town"),
    Some("test county"),
    Some("test post code"), Some("GB")
  )

  def viewmodel(line2: Option[String] = Some("address line 2")) = SameContactAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("hint"),
    address = testAddress(line2),
    psaName = "Test name",
    mode = NormalMode,
    displayReturnLink = true
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[SameContactAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(), UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider("error.required"), viewmodel(), countryOptions)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[SameContactAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
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

  def controllerPostDataChange(v:Boolean):Unit = {
    s"return a redirect when the submitted data is valid and the data is changed to $v" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          when(cacheConnector.save[Boolean, FakeIdentifier.type](
            eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, ContactAddressIdentifier.type](
            eqTo(ContactAddressIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> (if(v) "true" else "false")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers().set(FakeIdentifier)(!v).asOpt.value, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }
  }

  "post" must {

    behave like controllerPostDataChange(true)

    behave like controllerPostDataChange(false)

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
            eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, ContactAddressIdentifier.type](
            eqTo(ContactAddressIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          verify(cacheConnector, times(2)).save(any(), any())(any(), any(), any())
      }
    }

    "return a redirect and save the data when the there is existing data and the data is not changed" in {
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
            eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[Address, ContactAddressIdentifier.type](
            eqTo(ContactAddressIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers().set(FakeIdentifier)(true).asOpt.value, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          verify(cacheConnector, times(2)).save(any(), any())(any(), any(), any())
      }
    }

    "return a redirect when the submitted data is valid and address does not have line 2" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          when(cacheConnector.save[Boolean, FakeIdentifier.type](
            eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(cacheConnector.save[TolerantAddress, RegAddressIdentifier.type](
            eqTo(RegAddressIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(None), UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[SameContactAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider("error.required").bind(Map.empty[String, String]),
            viewmodel(),
            countryOptions
          )(request, messages).toString
      }
    }
  }
}
