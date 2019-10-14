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

import controllers.ControllerSpecBase
import forms.BusinessNameFormProvider
import identifiers.TypedIdentifier
import identifiers.register.{BusinessNameId, BusinessTypeId}
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.businessName

trait BusinessNameControllerBehaviour extends ControllerSpecBase {

  import BusinessNameControllerBehaviour._


  def businessNameController[I <: TypedIdentifier[String]](answers: UserAnswers,
                                                           createController: (UserAnswers) => BusinessNameController
                                                          ): Unit = {
    "CompanyName Controller" must {

      "return OK and the correct view for a GET" in {
        val result = createController(answers).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(testForm(), answers)
      }

      "populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = answers.set(BusinessNameId)(businessname).asOpt.value

        val result = createController(userAnswers).onPageLoad(NormalMode)(fakeRequest)

        contentAsString(result) mustBe viewAsString(form.fill(businessname), answers)
      }

      "redirect to the next page when valid data is submitted" in {
        val postRequest = testRequest(name = Some(businessname))

        val result = createController(answers).onSubmit(NormalMode)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)

      }

      "return a Bad Request and errors when invalid data is submitted" in {
        val postRequest = testRequest(name = Some("test ** invalid"))
        val boundForm = testForm().bindFromRequest()(postRequest)

        val result = createController(answers).onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm, answers)
      }

      "redirect to Session Expired for a GET if no existing data is found" in {
        val result = createController(UserAnswers()).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}

object BusinessNameControllerBehaviour extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new BusinessNameFormProvider()
  private val form = formProvider()
  private def businessType(answers: UserAnswers) = answers.get(BusinessTypeId).map {
    businessType =>
    messagesApi(s"businessType.${businessType.toString}").toLowerCase}
    .getOrElse("Missing business type in user answers")

  private val businessname = "test limited company"



  def testRequest(answers: UserAnswers = UserAnswers(), name: Option[String] = None): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request = name.map {
      nameValue =>
        fakeRequest.withFormUrlEncodedBody(
          "value" -> nameValue
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(UserType.Organisation, None, false, None),
      userAnswers = answers
    )

  }

  def testForm(): Form[String] =
    new BusinessNameFormProvider()()

  def viewAsString(form: Form[_],
                   answers: UserAnswers, href: Call = onwardRoute): String =
    businessName(frontendAppConfig, form, NormalMode, businessType(answers), href)(fakeRequest, messages).toString()

}