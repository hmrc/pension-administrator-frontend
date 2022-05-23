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

import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.BusinessNameId
import models.Mode
import models.register.{Task, TaskList}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DateHelper, UserAnswers}
import utils.annotations.AuthWithNoIV
import views.html.register.taskList

import javax.inject.Inject
import scala.concurrent.Future

class CompanyRegistrationTaskListController @Inject()(
                                                       val controllerComponents: MessagesControllerComponents,
                                                       @AuthWithNoIV authenticate: AuthAction,
                                                       allowAccess: AllowAccessActionProvider,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       taskList: taskList
                                                     )
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async { implicit request =>
    val expireAt = DateHelper.fromMillis(request.userAnswers.expireAt)
    Future.successful(Ok(taskList(buildTaskList(request.userAnswers), expireAt)))
  }

  private def buildTaskList(userAnswers: UserAnswers)(implicit messages: Messages): TaskList = {
    val businessName = userAnswers.get(BusinessNameId).fold(messages("site.company"))(identity)
    val basicDetails = Task(messages("taskList.basicDetails"), isCompleted = false)
    val companyDetails = Task(messages("taskList.companyDetails"), isCompleted = false)
    val contactDetails = Task(messages("taskList.contactDetails"), isCompleted = false)
    val directors = Task(messages("taskList.directors"), isCompleted = false)
    val workingKnowledgeDetails = Task(messages("taskList.workingKnowledgeDetails"), isCompleted = false)
    TaskList(businessName, List(basicDetails, companyDetails, contactDetails, directors, workingKnowledgeDetails))
  }
}
