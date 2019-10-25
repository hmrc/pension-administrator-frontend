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

package controllers.register

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.PhoneFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.phone

import scala.concurrent.Future

class PhoneControllerSpec extends SpecBase {

  import PhoneControllerSpec._

  val viewmodel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    mode = NormalMode,
    entityName = companyName
  )

  "get" must {

    "return a successful resultCompany when there is no existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual phone(frontendAppConfig, formProvider(), viewmodel)(FakeRequest(), messages).toString
      }
    }

    "return a successful resultCompany when there is an existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers().set(FakeIdentifier)("1234").get)

          status(result) mustEqual OK
          contentAsString(result) mustEqual phone(
            frontendAppConfig,
            formProvider().fill("1234"),
            viewmodel
          )(FakeRequest(), messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {
      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val request = FakeRequest().withFormUrlEncodedBody(
            ("value", "1234")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), FakeRequest())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual phone(
            frontendAppConfig,
            formProvider().bind(Map.empty[String, String]),
            viewmodel
          )(FakeRequest(), messages).toString
      }
    }
  }
}
object PhoneControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]
  val companyName = "test company name"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: PhoneFormProvider
                                ) extends PhoneController {

    def onPageLoad(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(), viewmodel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider(), viewmodel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }
}







