/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.{DataRetrievalAction, _}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.adviser.AdviserNameFormProvider
import identifiers.register.adviser.AdviserNameId
import models.NormalMode
import play.api.data.Form
import play.api.test.FakeRequest

import utils.{FakeNavigator, UserAnswers}
import views.html.register.adviser.adviserName

class AdviserNameControllerSpec extends ControllerWithQuestionPageBehaviours {

  val view: adviserName = app.injector.instanceOf[adviserName]
  val formProvider = new AdviserNameFormProvider()
  private val form = formProvider()
  private val userAnswer = UserAnswers().adviserName("test")
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("adviserName", "test"))

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, fakeAuth: AuthAction) = {

    new AdviserNameController(
      frontendAppConfig, fakeAuth, new FakeNavigator(onwardRoute), dataRetrievalAction,
      requiredDataAction, formProvider, FakeUserAnswersCacheConnector,
      controllerComponents, view).onPageLoad(NormalMode)
  }


  private def onSubmitAction(dataRetrievalAction: DataRetrievalAction, fakeAuth: AuthAction) = {

    new AdviserNameController(
      frontendAppConfig, fakeAuth, navigator, dataRetrievalAction,
      requiredDataAction, formProvider, FakeUserAnswersCacheConnector,
      controllerComponents, view).onSubmit(NormalMode)
  }

  private def viewAsString(form: Form[_]) = view(form, NormalMode, None)(fakeRequest, messages).toString


  behave like controllerWithOnPageLoadMethod(onPageLoadAction, getEmptyData, userAnswer.dataRetrievalAction, form, form.fill("test"), viewAsString)

  behave like controllerWithOnSubmitMethod(onSubmitAction, getEmptyData, form.bind(Map(AdviserNameId.toString -> "")), viewAsString, postRequest)

}

