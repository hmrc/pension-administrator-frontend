/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company._
import models.{Address, CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CheckYourAnswersFactory, CountryOptions}
import viewmodels.AnswerSection
import views.html.check_your_answers
import utils.CheckYourAnswers.Ops._

import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           checkYourAnswersFactory: CheckYourAnswersFactory,
                                           countryOptions: CountryOptions
                                          ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val checkYourAnswerHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)
      
      val companyDetails = AnswerSection(
        Some("company.checkYourAnswers.company.details.heading"),
        Seq(
          CompanyDetailsId.row(
            controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
          ),
          checkYourAnswerHelper.vatRegistrationNumber,
          checkYourAnswerHelper.payeEmployerReferenceNumber,
          CompanyRegistrationNumberId.row(
            controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url
          ),
          CompanyUniqueTaxReferenceId.row(
            controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url
          )
        ).flatten
      )

      val companyContactDetails = AnswerSection(
        Some("company.checkYourAnswers.company.contact.details.heading"),
        Seq(
          CompanyAddressId.row(
            controllers.register.company.routes.CompanyAddressController.onPageLoad().url
          ),
          CompanyAddressYearsId.row(
            controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url
          ),
          CompanyPreviousAddressId.row(
            controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url
          )
        ).flatten
      )

      val contactDetails = AnswerSection(
        Some("company.checkYourAnswers.contact.details.heading"),
        Seq(
          checkYourAnswerHelper.email,
          checkYourAnswerHelper.phone
        ).flatten
      )

      Ok(check_your_answers(
        appConfig,
        Seq(companyDetails, companyContactDetails, contactDetails),
        Some(messagesApi("site.secondaryHeader")),
        controllers.register.company.routes.CheckYourAnswersController.onSubmit()
      ))
  }

  def onSubmit: Action[AnyContent] = authenticate {
    implicit request =>
      Redirect(routes.AddCompanyDirectorsController.onPageLoad(NormalMode))
  }

  implicit def addressAnswer(address: Address): Seq[String] = {
    val country = countryOptions.options
      .find(_.value == address.country)
      .map(_.label)
      .getOrElse(address.country)

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postcode.map(postcode => s"$postcode,"),
      Some(country)
    ).flatten
  }
}
