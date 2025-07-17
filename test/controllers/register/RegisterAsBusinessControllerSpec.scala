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

package controllers.register

import audit.testdoubles.StubSuccessfulAuditService
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.*
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.RegisterAsBusinessFormProvider
import identifiers.register.RegisterAsBusinessId
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, *}
import utils.testhelpers.DataCompletionBuilder.*
import utils.{UserAnswerOps, UserAnswers}
import views.html.register.registerAsBusiness

class RegisterAsBusinessControllerSpec extends ControllerWithQuestionPageBehaviours {

  val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  val view: registerAsBusiness = app.injector.instanceOf[registerAsBusiness]

  private val auditService = new StubSuccessfulAuditService()

  val validData: UserAnswers = UserAnswers().registerAsBusiness(true)

  val postRequestTrue: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", true.toString))
  val postRequestFalse: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", false.toString))

  "RegisterAsBusinessController" must {

    behave like controllerWithOnPageLoadMethod(
      onPageLoadAction,
      getEmptyData,
      validData.dataRetrievalAction,
      form,
      form.fill(true),
      viewAsString
    )

    behave like controllerThatSavesUserAnswers(
      saveAction,
      postRequestTrue,
      RegisterAsBusinessId,
      true
    )

    "calling onSubmit" must {

      "return a Bad Request and errors when invalid data is submitted" in {

        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
          onSubmit()(fakeRequest)

        val formWithErrors = form.bind(Map.empty[String, String])

        status(result).mustBe(BAD_REQUEST)
        contentAsString(result).mustBe(viewAsString(formWithErrors))
      }

      "route to the company 'before you start' page when the registration is for a company / partnership" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
          onSubmit()(postRequestTrue)

        status(result).mustBe(SEE_OTHER)
        redirectLocation(result).mustBe(Some(routes.WhatYouWillNeedController.onPageLoad().url))
      }

      "route to the individual 'before you start' page when the registration is NOT for a company / partnership" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
          onSubmit()(postRequestFalse)

        status(result).mustBe(SEE_OTHER)
        redirectLocation(result).mustBe(Some(individual.routes.WhatYouWillNeedController.onPageLoad().url))
      }

      "send a PSAStart audit event" in {

        auditService.reset()

        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
          onSubmit()(postRequestTrue)

        status(result).mustBe(SEE_OTHER)
        auditService.verifyNothingSent().mustBe(false)
      }

      "when the registration is for a company / partnership" must {

        "route to 'continue with registration' page when the registration is for a UK company or partnership" in {
          val userAnswers = validData.completeCompanyDetailsUK

          val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
            onSubmit()(postRequestTrue)

          status(result).mustBe(SEE_OTHER)
          redirectLocation(result).mustBe(Some(routes.ContinueWithRegistrationController.onPageLoad().url))
        }

        "route to the company 'before you start' page when the registration is for a non-UK company" in {
          val userAnswers = validData.completeCompanyDetailsNonUKCustomer

          val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector).
            onSubmit()(postRequestTrue)

          status(result).mustBe(SEE_OTHER)
          redirectLocation(result).mustBe(Some(routes.WhatYouWillNeedController.onPageLoad().url))
        }
      }
    }
  }

  def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad(NormalMode)

  def onSubmitAction()(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onSubmit()

  def saveAction(cache: UserAnswersCacheConnector): Action[AnyContent] =
    controller(cache = cache).onSubmit()

  def viewAsString(form: Form[?]): String =
      view(form)(fakeRequest, messagesApi.preferred(fakeRequest)).toString()

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    authAction: AuthAction = FakeAuthAction,
    cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ): RegisterAsBusinessController =
    new RegisterAsBusinessController(
      messagesApi,
      authAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      cache,
      auditService,
      controllerComponents,
      view
    )

}
