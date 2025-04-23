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

package controllers.register.adviser

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.adviser.AdviserNameFormProvider
import models.RegistrationCustomerType.UK
import models.RegistrationLegalStatus.LimitedCompany
import models.{NormalMode, RegistrationIdType, RegistrationInfo}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.FakeRequest
import utils.{FakeNavigator, UserAnswers}
import views.html.register.adviser.adviserName

class AdviserNameControllerSpec extends ControllerWithQuestionPageBehaviours {

  val view: adviserName = app.injector.instanceOf[adviserName]
  val formProvider = new AdviserNameFormProvider()
  private val form = formProvider()
  val registrationInfo: RegistrationInfo = RegistrationInfo(LimitedCompany, "", noIdentifier = false, UK, Some(RegistrationIdType.Nino), Some("AB121212C"))
  private val validUserAnswer = UserAnswers().adviserName("test").businessName(companyName).registrationInfo(registrationInfo)
  private val emptyUserAnswer = UserAnswers().businessName(companyName).registrationInfo(registrationInfo)
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("adviserName", "test"))

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, fakeAuth: AuthAction) = {

    new AdviserNameController(
      fakeAuth, new FakeNavigator(onwardRoute), dataRetrievalAction,
      requiredDataAction, formProvider, FakeUserAnswersCacheConnector,
      controllerComponents, view
    ).onPageLoad(NormalMode)
  }


  private def onSubmitAction(dataRetrievalAction: DataRetrievalAction, fakeAuth: AuthAction) = {

    new AdviserNameController(
      fakeAuth, navigator, dataRetrievalAction,
      requiredDataAction, formProvider, FakeUserAnswersCacheConnector,
      controllerComponents, view
    ).onSubmit(NormalMode)
  }

  private def viewAsString(form: Form[?]) = view(
    form,
    NormalMode,
    Some(companyName),
    Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
  )(fakeRequest, messages).toString

  behave like controllerWithOnPageLoadMethod(onPageLoadAction, emptyUserAnswer.dataRetrievalAction, validUserAnswer.dataRetrievalAction, form, form.fill("test"), viewAsString)

  behave like controllerWithOnSubmitMethod(onSubmitAction, validUserAnswer.dataRetrievalAction, form.bind(Json.obj(), 0), viewAsString, postRequest)

}

