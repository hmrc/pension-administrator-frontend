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

import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register._
import identifiers.register.partnership._
import models.{Mode, NormalMode}
import identifiers.register.partnership.MoreThanTenPartnersId
import models.{Mode, NormalMode}
import models.register.{Task, TaskList}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import utils.dataCompletion.DataCompletion.isAdviserComplete
import utils.{DateHelper, UserAnswers}
import views.html.register.taskList

import javax.inject.Inject
import scala.concurrent.Future

class PartnershipRegistrationTaskListController @Inject()(
                                                           appConfig: FrontendAppConfig,
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
      buildPartnershipDetails(userAnswers),
      buildContactDetails(userAnswers),
      buildPartnersDetails(userAnswers),
      buildWorkingKnowledgeTask(userAnswers)
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
    val isValueEntered = userAnswers.get(HasPAYEId).getOrElse(false) || userAnswers.get(HasVATId).getOrElse(false)
    val url: String = if (isCompleted || isValueEntered) {
      partnershipDetails.routes.CheckYourAnswersController.onPageLoad().url
    } else {
      partnershipDetails.routes.WhatYouWillNeedController.onPageLoad().url
    }
    Task(messages("taskList.partnershipDetails"), isCompleted, url)
  }

    private def buildContactDetails(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
      val isContactAddressCompleted = userAnswers.get(PartnershipContactAddressId).isDefined
      val isEmailCompleted = userAnswers.get(PartnershipEmailId).isDefined
      val isPhoneCompleted = userAnswers.get(PartnershipPhoneId).isDefined
      val isCompleted = isContactAddressCompleted && isEmailCompleted && isPhoneCompleted
      val isValueEntered = isContactAddressCompleted || isEmailCompleted || isPhoneCompleted
      val url: String = if (isCompleted || isValueEntered){
        contactDetails.routes.CheckYourAnswersController.onPageLoad().url
      } else {
        contactDetails.routes.WhatYouWillNeedController.onPageLoad().url
      }
      Task(messages("taskList.contactDetails"), isCompleted, url)
    }

    private def buildWorkingKnowledgeTask(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
      val isWorkingKnowledgeCompleted = isAdviserComplete(userAnswers,NormalMode)

      val workingKnowledgeDetailsUrl = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode).url
      Task(messages("taskList.workingKnowledgeDetails"), isWorkingKnowledgeCompleted, workingKnowledgeDetailsUrl)
    }

  private def buildPartnersDetails(userAnswers: UserAnswers)(implicit messages: Messages): Task = {
    val isPartnersCompleted = userAnswers.allPartnersAfterDeleteV2(NormalMode).nonEmpty
    val partnerTaskUrl = if(isPartnersCompleted){
      controllers.register.administratorPartnership.routes.AddPartnerController.onPageLoad(NormalMode).url
    } else {
      controllers.register.administratorPartnership.partners.routes.WhatYouWillNeedController.onPageLoad().url
    }

    Task(messages("taskList.partners"), isCompleted = isPartnershipPartnersComplete(userAnswers), url = partnerTaskUrl)
  }

  def isPartnershipPartnersComplete(userAnswers: UserAnswers): Boolean = {
    val allPartners = userAnswers.allPartnersAfterDeleteV2(NormalMode)

    val allPartnersCompleted = allPartners.nonEmpty && allPartners.forall(_.isComplete) && allPartners.size >= 2 &&
      (allPartners.size < appConfig.maxPartners || userAnswers.get(MoreThanTenPartnersId).isDefined)

    allPartnersCompleted
  }
}
