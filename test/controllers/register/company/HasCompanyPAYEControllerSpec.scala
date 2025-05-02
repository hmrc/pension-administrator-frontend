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

package controllers.register.company

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import forms.HasReferenceNumberFormProvider
import identifiers.register.HasPAYEId
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber
import utils.UserAnswerOps

class HasCompanyPAYEControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("hasPAYE.error.required", companyName)

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private def viewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.company.routes.HasCompanyPAYEController.onSubmit(NormalMode),
      title = Message("hasPAYE.heading", Message("theCompany")),
      heading = Message("hasPAYE.heading", companyName),
      mode = NormalMode,
      hint = Some(Message("hasPAYE.hint")),
      entityName = companyName,
      returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )

  private def viewAsString(form: Form[?] = form, mode: Mode = NormalMode): String =
    view(form, viewModel)(fakeRequest, messagesApi.preferred(fakeRequest)).toString

  "HasCompanyPAYEController Controller" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(UserAnswers().businessName(companyName).dataRetrievalAction) ++
          Seq[GuiceableModule]()*)) {
          app =>
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val result = controller.onPageLoad(NormalMode)(fakeRequest)
            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "populate the view correctly when the question has previously been answered" in {
        val validData = UserAnswers().businessName(companyName).set(HasPAYEId)(value = true).asOpt.value.dataRetrievalAction
        running(_.overrides(modules(validData) ++
          Seq[GuiceableModule]()*)) {
          app =>
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val result = controller.onPageLoad(NormalMode)(fakeRequest)
            contentAsString(result) mustBe viewAsString(form.fill(value = true))
        }
      }

      "redirect to Session Expired if no existing data is found" in {
        running(_.overrides(modules(dontGetAnyData)*)) {
          app =>
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val result = controller.onPageLoad(NormalMode)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

    "on a POST" must {
      "redirect to the next page when valid data is submitted" in {
        running(_.overrides(
          modules(UserAnswers().businessName(companyName).dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector))*)) {
          app =>
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val result = controller.onSubmit(NormalMode)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        running(_.overrides(modules(UserAnswers().businessName(companyName).dataRetrievalAction) ++
          Seq[GuiceableModule]()*)) {
          app =>
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val boundForm = form.bind(Map("value" -> "invalid value"))
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val result = controller.onSubmit(NormalMode)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }

      "redirect to Session Expired if no existing data is found" in {
        running(_.overrides(modules(dontGetAnyData)*)) {
          app =>
            val controller = app.injector.instanceOf[HasCompanyPAYEController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
            val result = controller.onSubmit(NormalMode)(postRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }
  }
}
