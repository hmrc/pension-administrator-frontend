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
import identifiers.register.company.{PhoneId, _}
import identifiers.register._
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{Enumerable, Navigator}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @RegisterCompany navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>

      val companyDetails = AnswerSection(
        Some("company.checkYourAnswers.company.details.heading"),
        BusinessNameId.row(None)(request, implicitly) ++
          BusinessUTRId.row(None)
          ++ Seq(
          HasCompanyCRNId.row(Some(Link(routes.HasCompanyCRNController.onPageLoad(CheckMode).url))),
          CompanyRegistrationNumberId.row(Some(Link(routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url))),
          HasPAYEId.row(Some(Link(routes.HasCompanyPAYEController.onPageLoad(CheckMode).url))),
          EnterPAYEId.row(Some(Link(routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url))),
          HasVATId.row(Some(Link(routes.HasCompanyVATController.onPageLoad(CheckMode).url))),
          EnterVATId.row(Some(Link(routes.CompanyEnterVATController.onPageLoad(CheckMode).url)))
           ).flatten
      )

      val companyContactDetails = AnswerSection(
        Some("company.checkYourAnswers.company.contact.details.heading"),
        Seq(
          CompanyAddressId.row(None),
          CompanySameContactAddressId.row(Some(Link(routes.CompanySameContactAddressController.onPageLoad(CheckMode).url))),
          CompanyContactAddressId.row(None),
          CompanyAddressYearsId.row(Some(Link(routes.CompanyAddressYearsController.onPageLoad(CheckMode).url))),
          CompanyPreviousAddressId.row(Some(Link(routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)))
        ).flatten
      )

      val contactDetails = AnswerSection(
        headingKey = Some("common.checkYourAnswers.contact.details.heading"),
        rows = Seq(
          EmailId.row(Some(Link(routes.EmailController.onPageLoad(CheckMode).url))),
          PhoneId.row(Some(Link(routes.PhoneController.onPageLoad(CheckMode).url)))
        ).flatten
      )

      Ok(check_your_answers(
        appConfig,
        Seq(companyDetails, companyContactDetails, contactDetails),
        controllers.register.company.routes.CheckYourAnswersController.onSubmit(),
        None, mode
      ))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))

  }
}
