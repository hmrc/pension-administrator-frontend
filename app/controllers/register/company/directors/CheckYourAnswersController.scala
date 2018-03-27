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

package controllers.register.company.directors

import javax.inject.Inject

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company.directors._
import models.{Address, CheckMode, Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.CheckYourAnswers.Ops._
import utils.CountryOptions
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future
import scala.language.implicitConversions

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           countryOptions: CountryOptions) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      DirectorDetailsId(index).retrieve.right.map { director =>

        val directorDetails = DirectorDetailsId(index).row(
          controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url
        )
        val directorNino = DirectorNinoId(index).row(
          controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url
        )
        val directorUniqueTaxReference = DirectorUniqueTaxReferenceId(index).row(
          controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url
        )

        val directorAddress = DirectorAddressId(index).row(
          controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(CheckMode, index).url
        )
        val directorAddressYears = DirectorAddressYearsId(index).row(
          controllers.register.company.directors.routes.DirectorAddressYearsController.onPageLoad(CheckMode, index).url
        )
        val directorPreviousAddress = DirectorPreviousAddressId(index).row(
          controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index).url
        )
        val directorContactDetails = DirectorContactDetailsId(index).row(
          controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url
        )

        val answers = Seq(
          AnswerSection(
            Some("directorCheckYourAnswers.directorDetails.heading"),
            directorDetails ++ directorNino ++ directorUniqueTaxReference
          ),
          AnswerSection(
            Some("directorCheckYourAnswers.contactDetails.heading"),
            directorAddress ++ directorAddressYears ++ directorPreviousAddress ++ directorContactDetails
          )
        )

        Future.successful(Ok(check_your_answers(
          appConfig,
          answers,
          Some(director.fullName),
          controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit()))
        )
      }
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode))

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
