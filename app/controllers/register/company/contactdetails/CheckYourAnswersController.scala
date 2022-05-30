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

package controllers.register.company.contactdetails

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.company
import identifiers.register.company._
import models.NormalMode
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
      val nextPage = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()
      Ok(checkYourAnswersView(checkYourAnswersSummary(request.userAnswers), nextPage, None, NormalMode, isComplete = true))
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad())
  }

  private def checkYourAnswersSummary(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] = {
    val isCompanyTradingOverAYear = userAnswers.get(CompanyTradingOverAYearId).isDefined
    Seq(
      AnswerSection(None,
        CompanyContactAddressId.cya.row(CompanyContactAddressId)(Some(Link(company.routes.CompanySameContactAddressController.onPageLoad(NormalMode).url)), userAnswers) ++
          CompanyAddressYearsId.cya.row(CompanyAddressYearsId)(Some(Link(company.routes.CompanyAddressYearsController.onPageLoad(NormalMode).url)), userAnswers) ++
          (if (isCompanyTradingOverAYear) CompanyTradingOverAYearId.cya.row(CompanyTradingOverAYearId)(Some(Link(company.routes.CompanyTradingOverAYearController.onPageLoad(NormalMode).url)), userAnswers) else Nil) ++
          CompanyEmailId.cya.row(CompanyEmailId)(Some(Link(company.routes.CompanyEmailController.onPageLoad(NormalMode).url)), userAnswers) ++
          CompanyPhoneId.cya.row(CompanyPhoneId)(Some(Link(company.routes.CompanyPhoneController.onPageLoad(NormalMode).url)), userAnswers)
      )
    )
  }
}
