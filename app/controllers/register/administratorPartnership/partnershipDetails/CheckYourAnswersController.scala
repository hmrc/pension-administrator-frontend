/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partnershipDetails

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.{BusinessNameId, EnterPAYEId, EnterVATId, HasPAYEId, HasVATId}
import models.{CheckMode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoPDV
import viewmodels.{AnswerSection, Link, Message, Section}
import views.html.check_your_answers

import javax.inject.Inject

class CheckYourAnswersController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            @AuthWithNoPDV authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            checkYourAnswersView: check_your_answers
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val sections: Seq[Section] = Seq(
        AnswerSection(None,
          HasPAYEId.cya.row(HasPAYEId)(Some(Link(routes.HasPartnershipPAYEController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            EnterPAYEId.cya.row(EnterPAYEId)(Some(Link(routes.PartnershipEnterPAYEController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            HasVATId.cya.row(HasVATId)(Some(Link(routes.HasPartnershipVATController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            EnterVATId.cya.row(EnterVATId)(Some(Link(routes.PartnershipEnterVATController.onPageLoad(CheckMode).url)), request.userAnswers)
        )
      )
      val nextPage = controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad()
      val partnershipName = request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership").resolve)

      Ok(checkYourAnswersView(sections, nextPage, None, NormalMode, isComplete = true, Some(partnershipName)))
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad())
  }

}
