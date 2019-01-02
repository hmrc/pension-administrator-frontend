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

package controllers.register.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company._
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @RegisterCompany navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val companyDetails = AnswerSection(
        Some("company.checkYourAnswers.company.details.heading"),
        BusinessDetailsId.row(None)
          ++ Seq(
          CompanyDetailsId.row(Some(routes.CompanyDetailsController.onPageLoad(CheckMode).url)),
          CompanyRegistrationNumberId.row(Some(routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url))
        ).flatten
      )

      val companyContactDetails = AnswerSection(
        Some("company.checkYourAnswers.company.contact.details.heading"),
        Seq(
          CompanyAddressId.row(None),
          CompanySameContactAddressId.row(Some(routes.CompanySameContactAddressController.onPageLoad(CheckMode).url)),
          CompanyContactAddressId.row(None),
          CompanyAddressYearsId.row(Some(routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
          CompanyPreviousAddressId.row(Some(routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url))
        ).flatten
      )

      val contactDetails = AnswerSection(
        Some("common.checkYourAnswers.contact.details.heading"),
        ContactDetailsId.row(Some(routes.ContactDetailsController.onPageLoad(CheckMode).url))
      )

      Ok(check_your_answers(
        appConfig,
        Seq(companyDetails, companyContactDetails, contactDetails),
        None,
        controllers.register.company.routes.CheckYourAnswersController.onSubmit()
      ))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))

  }
}
