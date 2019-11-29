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

package controllers

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import forms.HasReferenceNumberFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.hasReferenceNumber

import scala.concurrent.{ExecutionContext, Future}

class HasReferenceNumberControllerSpec extends SpecBase {

  import HasReferenceNumberControllerSpec._

  "get" must {

    "return a successful result when there is no existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[HasReferenceNumberFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewModel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            hasRefView(formProvider(requiredError, entityName), viewModel)(FakeRequest(), messagesApi.preferred(fakeRequest)).toString
      }
    }

    "return a successful result when there is an existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[HasReferenceNumberFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewModel, UserAnswers().set(FakeIdentifier)(value = true).get)

          status(result) mustEqual OK
          contentAsString(result) mustEqual hasRefView(
            formProvider(requiredError, entityName).fill(value = true),
            viewModel
          )(FakeRequest(), messagesApi.preferred(fakeRequest)).toString
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
            ("value", "true")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewModel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[HasReferenceNumberFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual hasRefView(
            formProvider(requiredError, entityName).bind(Map.empty[String, String]),
            viewModel
          )(FakeRequest(), messagesApi.preferred(fakeRequest)).toString
      }
    }
  }
}

object HasReferenceNumberControllerSpec extends ControllerSpecBase {
  private val entityName = "entity name"
  val requiredError = "error.required"
  private val viewModel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    mode = NormalMode,
    entityName = entityName
  )

  val hasRefView: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  object FakeIdentifier extends TypedIdentifier[Boolean]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: HasReferenceNumberFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: hasReferenceNumber
                                )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

    def onPageLoad(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(requiredError, entityName), viewmodel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider(requiredError, entityName), viewmodel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    override protected def view: hasReferenceNumber = hasRefView

  }

}







