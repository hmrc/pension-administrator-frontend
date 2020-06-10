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

package controllers.register.individual

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class IndividualEmailControllerSpec extends ControllerWithCommonBehaviour {
  import IndividualEmailControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val view: email = app.injector.instanceOf[email]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new IndividualEmailController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider, stubMessagesControllerComponents(), view)



  private def emailView(form: Form[_] = emailForm): String = view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  "IndividualEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getIndividual,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }

}

object IndividualEmailControllerSpec {
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  private def viewModel(mode: Mode)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.IndividualEmailController.onSubmit(mode),
      title = Message("individual.email.title"),
      heading = Message("individual.email.title"),
      mode = mode,
      entityName = Message("common.you")
    )
}

