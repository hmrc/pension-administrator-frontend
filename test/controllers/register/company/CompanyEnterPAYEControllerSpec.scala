/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.company

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import forms.EnterPAYEFormProvider
import identifiers.register.EnterPAYEId
import models.NormalMode
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterPAYE

class CompanyEnterPAYEControllerSpec extends ControllerSpecBase {

  private val companyName = "test company"

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new EnterPAYEFormProvider()
  private val form = formProvider(companyName)

  val view: enterPAYE = app.injector.instanceOf[enterPAYE]

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyEnterPAYEController.onSubmit(NormalMode),
      title = Message("enterPAYE.heading", Message("theCompany")),
      heading = Message("enterPAYE.heading", companyName),
      hint = Some(Message("enterPAYE.hint")),
      mode = NormalMode,
      entityName = companyName
    )

  private val payeNumber = "123AB456"

  private def viewAsString(form: Form[_] = form): String = view(
    form,
    viewModel
  )(fakeRequest, messagesApi.preferred(fakeRequest)).toString

  "CompanyRegistrationNumber Controller" when {

    "on a GET" must {
      "return OK and the correct view for a GET" in {
        running(
          _.overrides(modules(UserAnswers().businessName().dataRetrievalAction): _*)
        ) {
          app =>
            val controller = app.injector.instanceOf[CompanyEnterPAYEController]
            val result = controller.onPageLoad(NormalMode)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "redirect to Session Expired if no existing data is found" in {
        running(
          _.overrides(modules(dontGetAnyData): _*)
        ) {
          app =>
            val controller = app.injector.instanceOf[CompanyEnterPAYEController]
            val result = controller.onPageLoad(NormalMode)(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

    "on a POST" must {
      "return a redirect when the submitted data is valid" in {
        running(_.overrides(modules(UserAnswers().businessName().dataRetrievalAction) ++
          Seq[GuiceableModule](bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind(classOf[Navigator]).qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute))) : _*
        )) {
          app =>
            val request = FakeRequest().withFormUrlEncodedBody(("value", payeNumber))
            val controller = app.injector.instanceOf[CompanyEnterPAYEController]
            val result = controller.onSubmit(NormalMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            FakeUserAnswersCacheConnector.verify(EnterPAYEId, payeNumber)
        }
      }

      "redirect to Session Expired if no existing data is found" in {
        running(
          _.overrides(modules(dontGetAnyData): _*)
        ) {
          app =>
            val request = FakeRequest().withFormUrlEncodedBody(("value", payeNumber))
            val controller = app.injector.instanceOf[CompanyEnterPAYEController]
            val result = controller.onSubmit(NormalMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }
  }
}
