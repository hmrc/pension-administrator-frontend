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

package controllers.register.company.directors

import connectors.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.register.NINOFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterNINO

class DirectorEnterNINOControllerSpec extends ControllerWithCommonBehaviour {

  import DirectorEnterNINOControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val ninoForm = formProvider(directorName)

  private def controller(dataRetrievalAction: DataRetrievalAction) = new DirectorEnterNINOController(
    new FakeNavigator(onwardRoute), frontendAppConfig, messagesApi, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  private def enterNINOView(form: Form[_] = ninoForm): String = enterNINO(frontendAppConfig, form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "DirectorEnterNINOController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getDirector,
      viewAsString = enterNINOView,
      form = ninoForm,
      request = postRequest
    )
  }
}

object DirectorEnterNINOControllerSpec {
  private val formProvider = new NINOFormProvider()
  private val index = 0
  private val directorName = "test first name test middle name test last name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "AB100100A"))

  private def viewModel(mode: Mode, index: Index)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorEnterNINOController.onSubmit(mode, index),
      title = Message("enterNINO.heading", Message("theDirector").resolve),
      heading = Message("enterNINO.heading", directorName),
      mode = mode,
      entityName = directorName
    )
}
