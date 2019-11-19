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

import audit.testdoubles.StubSuccessfulAuditService
import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.RegisterAsBusinessFormProvider
import identifiers.register.RegisterAsBusinessId
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.register.registerAsBusiness
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class RegisterAsBusinessControllerSpec extends ControllerWithQuestionPageBehaviours {

  import RegisterAsBusinessControllerSpec._

  "RegisterAsBusinessController" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction(this),
      getEmptyData,
      validData.dataRetrievalAction,
      form,
      form.fill(true),
      viewAsString(this)(form)
    )

    behave like controllerWithOnSubmitMethod(
      onSubmitAction(this, navigator),
      validData.dataRetrievalAction,
      form.bind(Map.empty[String, String]),
      viewAsString(this)(form),
      postRequest
    )

    behave like controllerThatSavesUserAnswers(
      saveAction(this),
      postRequest,
      RegisterAsBusinessId,
      true
    )

    "send a PSAStart audit event" in {

      auditService.reset()

      val result = controller(this)(validData.dataRetrievalAction, FakeAuthAction, FakeNavigator, FakeUserAnswersCacheConnector).
        onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      auditService.verifyNothingSent() mustBe false
    }

  }

}

object RegisterAsBusinessControllerSpec {

  val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  private val auditService = new StubSuccessfulAuditService()

  val validData: UserAnswers = UserAnswers().registerAsBusiness(true)

  val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", true.toString))

  def onPageLoadAction(base: ControllerSpecBase)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  def onSubmitAction(base: ControllerSpecBase, navigator: Navigator)(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(base)(dataRetrievalAction, authAction, navigator).onSubmit(NormalMode)

  def saveAction(base: ControllerSpecBase)(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(base)(cache = cache).onSubmit(NormalMode)

  def viewAsString(base: SpecBase)(form: Form[_]): Form[_] => String =
    form =>
      registerAsBusiness(
        base.frontendAppConfig,
        form
      )(base.fakeRequest, base.messages).toString()

  private def controller(base: ControllerSpecBase)(
    dataRetrievalAction: DataRetrievalAction = base.getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    navigator: Navigator = FakeNavigator,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): RegisterAsBusinessController =
    new RegisterAsBusinessController(
      base.frontendAppConfig,
      base.messagesApi,
      authAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      cache,
      navigator,
      auditService
    )

}
