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

package controllers.register.company.contactdetails

import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.company
import identifiers.register.company.*
import models.admin.ukResidencyToggle
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AuthWithNoIV
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerSection, Link, Section}
import views.html.check_your_answers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            @AuthWithNoIV authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            featureFlagService: FeatureFlagService,
                                            checkYourAnswersView: check_your_answers
                                          )
                                          (implicit countryOptions: CountryOptions,
                                           val executionContext: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Retrievals {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).flatMap { ukResidency =>
        val nextPage = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()
        Future.successful(Ok(checkYourAnswersView(checkYourAnswersSummary(request.userAnswers, ukResidency.isEnabled), nextPage,
          Some(companyName), NormalMode, isComplete = true, returnLink = taskListReturnLinkUrl())))
      }
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad())
  }

  private def checkYourAnswersSummary(userAnswers: UserAnswers, ukResidency: Boolean)(implicit messages: Messages): Seq[Section] = {
    import company.routes.*
    val isCompanyTradingOverAYear = userAnswers.get(CompanyTradingOverAYearId).isDefined
    val companyContactAddress = if (ukResidency) {
      CompanyUKContactAddressId.cya.row(CompanyUKContactAddressId)
    }
    else {
      CompanyContactAddressId.cya.row(CompanyContactAddressId)
    }
    Seq(
      AnswerSection(None,
        companyContactAddress(Some(Link(CompanySameContactAddressController.onPageLoad(CheckMode).url)), userAnswers) ++
          CompanyAddressYearsId.cya.row(CompanyAddressYearsId)(Some(Link(CompanyAddressYearsController.onPageLoad(CheckMode).url)), userAnswers) ++
          (if (isCompanyTradingOverAYear) {
            CompanyTradingOverAYearId.cya.row(CompanyTradingOverAYearId)(Some(Link(CompanyTradingOverAYearController.onPageLoad(CheckMode).url)), userAnswers)
          } else {
            Nil
          }) ++
          CompanyPreviousAddressId.cya.row(CompanyPreviousAddressId)(Some(Link(CompanyPreviousAddressController.onPageLoad(CheckMode).url)), userAnswers) ++
          CompanyEmailId.cya.row(CompanyEmailId)(Some(Link(CompanyEmailController.onPageLoad(CheckMode).url)), userAnswers) ++
          CompanyPhoneId.cya.row(CompanyPhoneId)(Some(Link(CompanyPhoneController.onPageLoad(CheckMode).url)), userAnswers)
      )
    )
  }
}
