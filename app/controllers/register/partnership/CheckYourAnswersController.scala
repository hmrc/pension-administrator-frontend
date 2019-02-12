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

package controllers.register.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.partnership.{CheckYourAnswersId, _}
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Partnership
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext


class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @Partnership navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            implicit val countryOptions: CountryOptions
                                          )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val partnershipDetails = AnswerSection(
        Some("checkyouranswers.partnership.details"),
        Seq(
          PartnershipDetailsId.row(None),
          PartnershipPayeId.row(Some(Link(routes.PartnershipPayeController.onPageLoad(CheckMode).url))),
          PartnershipVatId.row(Some(Link(routes.PartnershipVatController.onPageLoad(CheckMode).url)))
        ).flatten
      )

      val partnershipContactDetails = AnswerSection(
        Some("checkyouranswers.partnership.contact.details.heading"),
        Seq(
          PartnershipRegisteredAddressId.row(None),
          PartnershipSameContactAddressId.row(Some(Link(routes.PartnershipSameContactAddressController.onPageLoad(CheckMode).url))),
          PartnershipContactAddressId.row(Some(Link(routes.PartnershipContactAddressController.onPageLoad(CheckMode).url))),
          PartnershipAddressYearsId.row(Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url))),
          PartnershipPreviousAddressId.row(Some(Link(routes.PartnershipPreviousAddressController.onPageLoad(CheckMode).url)))
        ).flatten
      )

      val contactDetails = AnswerSection(
        Some("common.checkYourAnswers.contact.details.heading"),
        Seq(
          PartnershipContactDetailsId.row(Some(Link(routes.PartnershipContactDetailsController.onPageLoad(CheckMode).url)))
        ).flatten
      )

      Ok(check_your_answers(
        appConfig,
        Seq(partnershipDetails, partnershipContactDetails, contactDetails),
        None,
        controllers.register.partnership.routes.CheckYourAnswersController.onSubmit()
      ))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))

  }
}
