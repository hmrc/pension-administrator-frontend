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

package controllers.register.company.directors

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EnterUTRFormProvider
import models.FeatureToggle.Enabled
import models.FeatureToggleName.PsaRegistration
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterUTR

class DirectorEnterUTRControllerSpec extends ControllerWithCommonBehaviour {

  import DirectorEnterUTRControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val utrForm = formProvider(directorName)
  val view: enterUTR = app.injector.instanceOf[enterUTR]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new DirectorEnterUTRController(
    new FakeNavigator(onwardRoute),
    frontendAppConfig,
    FakeUserAnswersCacheConnector,
    FakeAuthAction,
    FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction,
    new DataRequiredActionImpl,
    formProvider,
    controllerComponents,
    view,
    FakeFeatureToggleConnector.returns(Enabled(PsaRegistration))
  )

  private def enterUTRView(form: Form[_]): String = view(form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "DirectorEnterUTRController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getDirector,
      viewAsString = enterUTRView,
      form = utrForm,
      request = postRequest
    )
  }
}

object DirectorEnterUTRControllerSpec {
  private val formProvider = new EnterUTRFormProvider()
  private val index = 0
  private val directorName = "test first name test last name"
  private val companyName = "Test Company Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "1111111111"))

  private def viewModel(mode: Mode, index: Index) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorEnterUTRController.onSubmit(mode, index),
      title = Message("enterUTR.heading", Message("theDirector")),
      heading = Message("enterUTR.heading", directorName),
      mode = mode,
      entityName = companyName,
      returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )
}
