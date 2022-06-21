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

package controllers.register.administratorPartnership

import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register._
import models.Mode
import models.register.{Task, TaskList}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import utils.{DateHelper, UserAnswers}
import views.html.register.taskList

import javax.inject.Inject
import scala.concurrent.Future

class PartnershipRegistrationTaskListController @Inject()(
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
    val businessName = userAnswers.get(BusinessNameId).fold(messages("site.partnership"))(identity)
    val declarationUrl = controllers.register.routes.DeclarationFitAndProperController.onPageLoad().url

    TaskList(businessName, declarationUrl, List(
      buildBasicDetailsTask(userAnswers),
      buildPartnershipDetails(userAnswers)
      //      buildContactDetails(userAnswers),
      //      buildDirectorDetails(userAnswers),
      //      buildWorkingKnowledgeTask(userAnswers))
    ))
  }

  private def buildBasicDetailsTask(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
    val isCompleted = userAnswers.get(RegistrationInfoId).isDefined
    val url = routes.BusinessMatchingCheckYourAnswersController.onPageLoad().url
    Task(messages("taskList.basicDetails"), isCompleted, url, viewOnly = true)
  }

  private def buildPartnershipDetails(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
    val isPAYECompleted = userAnswers.get(HasPAYEId).exists(if (_) userAnswers.get(EnterPAYEId).isDefined else true)
    val isVATCompleted = userAnswers.get(HasVATId).exists(if (_) userAnswers.get(EnterVATId).isDefined else true)
    val isCompleted = isPAYECompleted && isVATCompleted
    val url: String = if (isCompleted) {
      partnershipdetails.routes.CheckYourAnswersController.onPageLoad().url
    } else {
      partnershipdetails.routes.WhatYouWillNeedController.onPageLoad().url
    }
    Task(messages("taskList.partnershipDetails"), isCompleted, url)
  }
  //
  //  private def buildContactDetails(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
  //    val isContactAddressCompleted = userAnswers.get(PartnershipContactAddressId).isDefined
  //    val isEmailCompleted = userAnswers.get(PartnershipEmailId).isDefined
  //    val isPhoneCompleted = userAnswers.get(PartnershipPhoneId).isDefined
  //    val isCompleted = isContactAddressCompleted && isEmailCompleted && isPhoneCompleted
  //    val url = if(isCompleted){
  //      contactdetails.routes.CheckYourAnswersController.onPageLoad().url
  //    } else {
  //      contactdetails.routes.WhatYouWillNeedController.onPageLoad().url
  //    }
  //    Task(messages("taskList.contactDetails"), isCompleted, url)
  //  }
  //
  //  private def buildWorkingKnowledgeTask(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
  //    val isWorkingKnowledgeCompleted = isAdviserComplete(userAnswers,NormalMode)
  //
  //    val workingKnowledgeDetailsUrl = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode).url
  //    Task(messages("taskList.workingKnowledgeDetails"), isWorkingKnowledgeCompleted, workingKnowledgeDetailsUrl)
  //  }
  //
  //  private def buildDirectorDetails(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
  //    val isDirectorsDetailsCompleted = userAnswers.allDirectorsAfterDelete(NormalMode).nonEmpty
  //    val directorTaskUrl = if(isDirectorsDetailsCompleted){
  //      controllers.register.partnership.routes.AddPartnershipDirectorsController.onPageLoad(NormalMode).url
  //    } else {
  //      controllers.register.partnership.directors.routes.WhatYouWillNeedController.onPageLoad().url
  //    }
  //
  //    Task(messages("taskList.directors"), isCompleted = isEstablisherPartnershipAndDirectorsComplete(userAnswers), url = directorTaskUrl)
  //  }
  //
  //  def isEstablisherPartnershipAndDirectorsComplete(userAnswers: UserAnswers): Boolean = {
  //    val allDirectors = userAnswers.allDirectorsAfterDelete(NormalMode)
  //
  //    val allDirectorsCompleted = allDirectors.nonEmpty && allDirectors.forall(_.isComplete) &&
  //      (allDirectors.size < 10 || userAnswers.get(MoreThanTenDirectorsId).isDefined)
  //
  //      allDirectorsCompleted
  //  }
}
