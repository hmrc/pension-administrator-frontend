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

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeUnAuthorisedAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.YesNoFormProvider
import identifiers.register.{BusinessTypeId, RegistrationInfoId}
import models._
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.UserAnswers
import utils.testhelpers.DataCompletionBuilder._
import views.html.register.continueWithRegistration

class ContinueWithRegistrationControllerSpec extends ControllerWithQuestionPageBehaviours {

  import ContinueWithRegistrationControllerSpec._

  private val view: continueWithRegistration = app.injector.instanceOf[continueWithRegistration]

  "ContinueWithRegistrationController" must {

    "onPageLoad" must {

      "return OK and the correct view for a GET if the user has completed business matching" in {
        val userAnswers = validData.completeCompanyDetailsUK
        val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(form)
      }

      "return 303 if user action is not authenticated" in {

        val result = onPageLoadAction(getEmptyData, FakeUnAuthorisedAction())(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
      }

      "return 303 and redirect to 'what to register' if the user hasn't completed business matching" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RegisterAsBusinessController.onPageLoad().url)
      }
    }

    "onSubmit" must {
      "redirect to 'company registration task list' page when form value is true for UK company" in {
        val userAnswers = validData
          .setOrException(BusinessTypeId)(LimitedCompany)
          .setOrException(RegistrationInfoId)(registrationInfo(RegistrationCustomerType.UK))

        val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestTrue)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
      }

      "redirect to 'partnership registration task list' page when form value is true for UK partnership" in {
        val userAnswers = validData
          .setOrException(BusinessTypeId)(BusinessPartnership)
          .setOrException(RegistrationInfoId)(registrationInfo(RegistrationCustomerType.UK))

        val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestTrue)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
      }

      "redirect to WYWN page when form value is true when customer type is NON UK" in {
        val userAnswers = validData
          .setOrException(RegistrationInfoId)(registrationInfo(RegistrationCustomerType.NonUK))

        val result = controller(userAnswers.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestTrue)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatYouWillNeedController.onPageLoad(NormalMode).url)
      }

      "redirect to company 'before you begin' page when form value is false" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestFalse)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatYouWillNeedController.onPageLoad(NormalMode).url)
      }
    }
  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction,
                          authAction: AuthAction,
                          cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
                        ): ContinueWithRegistrationController =
    new ContinueWithRegistrationController(
      controllerComponents,
      authAction,
      dataRetrievalAction,
      view,
      new YesNoFormProvider(),
      cache
    )

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad()

  private def viewAsString(form: Form[?]): String =
    view(form)(fakeRequest, messagesApi.preferred(fakeRequest)).toString()
}

object ContinueWithRegistrationControllerSpec {

  private val utr = "test-utr"
  private val sapNumber = "test-sap-number"

  private def registrationInfo(registrationCustomerType: RegistrationCustomerType) = RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany,
    sapNumber,
    noIdentifier = false,
    registrationCustomerType,
    Some(RegistrationIdType.UTR),
    Some(utr)
  )


  private val form: Form[Boolean] = new YesNoFormProvider().apply()
  private val validData: UserAnswers = UserAnswers()
  private val postRequestTrue: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", true.toString))
  private val postRequestFalse: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", false.toString))

}
