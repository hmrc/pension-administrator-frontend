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

package controllers.register.administratorPartnership.contactDetails

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.partnership._
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AuthWithNoIV
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerSection, Link, Section}
import views.html.check_your_answers

import javax.inject.Inject

class CheckYourAnswersController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            @AuthWithNoIV authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            checkYourAnswersView: check_your_answers
                                          )(implicit countryOptions: CountryOptions) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val nextPage = controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad()
      Ok(checkYourAnswersView(checkYourAnswersSummary(request.userAnswers), nextPage, None, NormalMode, isComplete = true))
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad())
  }

  private def checkYourAnswersSummary(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] = {
    val isPartnershipTradingOverAYear = userAnswers.get(PartnershipTradingOverAYearId).isDefined
    Seq(
      AnswerSection(None,
        PartnershipContactAddressId.cya.row(PartnershipContactAddressId)(Some(Link(routes.PartnershipSameContactAddressController.onPageLoad(CheckMode).url)), userAnswers) ++
          PartnershipAddressYearsId.cya.row(PartnershipAddressYearsId)(Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)), userAnswers) ++
          (if (isPartnershipTradingOverAYear) {
            PartnershipTradingOverAYearId.cya.row(PartnershipTradingOverAYearId)(Some(Link(routes.PartnershipTradingOverAYearController.onPageLoad(CheckMode).url)), userAnswers)
          } else {
            Nil
          }) ++
          PartnershipPreviousAddressId.cya.row(PartnershipPreviousAddressId)(Some(Link(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)), userAnswers) ++
          PartnershipEmailId.cya.row(PartnershipEmailId)(Some(Link(routes.PartnershipEmailController.onPageLoad(CheckMode).url)), userAnswers) ++
          PartnershipPhoneId.cya.row(PartnershipPhoneId)(Some(Link(routes.PartnershipPhoneController.onPageLoad(CheckMode).url)), userAnswers)
      )
    )
  }
}
