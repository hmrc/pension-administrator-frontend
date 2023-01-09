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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models.NormalMode
import models.register.{Task, TaskList}
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.taskList

class CompanyRegistrationTaskListControllerSpec extends ControllerSpecBase {

  private val view: taskList = app.injector.instanceOf[taskList]
  private val validData = UserAnswers()
  private val expiryDateMillis = 1653326181964L
  private val expiryDate = "23 May 2022"

  "CompanyRegistrationTaskListController" must {
    "onPageLoad" must {
      "return OK and the correct view for a GET" in {
        val userAnswers = validData.businessName(companyName).setExpiryDate(expiryDateMillis).asOpt.value
        val result = controller(userAnswers.dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

        val expectedTaskList = TaskList(companyName, "", List(
          Task(messages("taskList.basicDetails"), isCompleted = false,
            "/register-as-pension-scheme-administrator/register/company/business-matching/check-your-answers"),
          Task(messages("taskList.companyDetails"), isCompleted = false,
            "/register-as-pension-scheme-administrator/register/company/details-what-you-will-need"),
          Task(messages("taskList.contactDetails"), isCompleted = false,
            "/register-as-pension-scheme-administrator/register/company/contact-what-you-will-need"),
          Task(messages("taskList.directors"), isCompleted = false,
            "/register-as-pension-scheme-administrator/register/company/directors/what-you-will-need"),
          Task(messages("taskList.workingKnowledgeDetails"), isCompleted = false,
            "/register-as-pension-scheme-administrator/register/working-knowledge-pensions")
        ))

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(expectedTaskList)
      }
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new CompanyRegistrationTaskListController(
      controllerComponents,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      view
    )

  private def viewAsString(taskList: TaskList): String =
    view(taskList, expiryDate)(fakeRequest, messagesApi.preferred(fakeRequest)).toString()
}
