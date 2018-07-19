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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.partnership.partners._
import javax.inject.Inject
import models.{CheckMode, Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.PartnershipPartner
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{Navigator, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @PartnershipPartner navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            sectionComplete: SectionComplete,
                                            implicit val countryOptions: CountryOptions
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnerName(index) { partnerName =>
        val answersSection = Seq(
          AnswerSection(
            Some("partnerCheckYourAnswers.partnerDetails.heading"),
            PartnerDetailsId(index).row(Some(routes.PartnerDetailsController.onPageLoad(CheckMode, index).url)) ++
              PartnerNinoId(index).row(Some(routes.PartnerNinoController.onPageLoad(CheckMode, index).url)) ++
              PartnerUniqueTaxReferenceId(index).row(Some(routes.PartnerUniqueTaxReferenceController.onPageLoad(CheckMode, index).url))
          ),
          AnswerSection(
            Some("partnerCheckYourAnswers.contactDetails.heading"),
            PartnerAddressId(index).row(Some(routes.PartnerAddressController.onPageLoad(CheckMode, index).url)) ++
              PartnerAddressYearsId(index).row(Some(routes.PartnerAddressYearsController.onPageLoad(CheckMode, index).url)) ++
              PartnerPreviousAddressId(index).row(None) ++
              PartnerContactDetailsId(index).row(Some(routes.PartnerContactDetailsController.onPageLoad(CheckMode, index).url))
          ))

        Future.successful(Ok(check_your_answers(
          appConfig,
          answersSection,
          Some(partnerName),
          routes.CheckYourAnswersController.onSubmit(index)))
        )
      }
  }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setComplete(IsPartnerCompleteId(index), request.userAnswers) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      }
  }

}
