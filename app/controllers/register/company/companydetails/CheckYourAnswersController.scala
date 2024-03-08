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

package controllers.register.company.companydetails

import connectors.cache.FeatureToggleConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.company
import identifiers.register.company.{CompanyRegistrationNumberId, HasCompanyCRNId}
import identifiers.register.{EnterPAYEId, EnterVATId, HasPAYEId, HasVATId}
import models.FeatureToggleName.PsaRegistration
import models.{CheckMode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import viewmodels.{AnswerSection, Link, Section}
import views.html.check_your_answers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            @AuthWithNoIV authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            checkYourAnswersView: check_your_answers,
                                            featureToggleConnector: FeatureToggleConnector
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val sections: Seq[Section] = Seq(
        AnswerSection(None,
          HasCompanyCRNId.cya.row(HasCompanyCRNId)(Some(
            Link(company.routes.HasCompanyCRNController.onPageLoad(CheckMode).url)), request.userAnswers) ++
          CompanyRegistrationNumberId.cya.row(CompanyRegistrationNumberId)(Some(
            Link(company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            HasPAYEId.cya.row(HasPAYEId)(Some(Link(company.routes.HasCompanyPAYEController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            EnterPAYEId.cya.row(EnterPAYEId)(Some(Link(company.routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            HasVATId.cya.row(HasVATId)(Some(Link(company.routes.HasCompanyVATController.onPageLoad(CheckMode).url)), request.userAnswers) ++
            EnterVATId.cya.row(EnterVATId)(Some(Link(company.routes.CompanyEnterVATController.onPageLoad(CheckMode).url)), request.userAnswers)
        )
      )
      val nextPage = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()
      featureToggleConnector.enabled(PsaRegistration).ifA (
        ifTrue = Ok(checkYourAnswersView(sections, nextPage, Some(companyName), NormalMode, isComplete = true, returnLink = taskListReturnLinkUrl())),
        ifFalse = Ok(checkYourAnswersView(sections, nextPage, None, NormalMode, isComplete = true))
      )
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad())
  }
}
