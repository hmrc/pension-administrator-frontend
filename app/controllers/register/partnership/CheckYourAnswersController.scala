/*
 * Copyright 2020 HM Revenue & Customs
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
import identifiers.register._
import identifiers.register.partnership.{CheckYourAnswersId, _}
import javax.inject.Inject
import models.requests.DataRequest
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import utils.annotations.Partnership
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext


class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @Partnership navigator: Navigator,
                                            implicit val countryOptions: CountryOptions,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: check_your_answers
                                          )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      if (isMandatoryDataPresent(request.userAnswers)) {
        loadCyaPage(mode)
      } else {
        Redirect(controllers.register.routes.RegisterAsBusinessController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val isDataComplete = request.userAnswers.isPartnershipDetailsComplete
      if (isDataComplete) {
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      } else {
        loadCyaPage(mode)
      }
  }

  private def isMandatoryDataPresent(userAnswers: UserAnswers): Boolean = {
    if (userAnswers.get(AreYouInUKId).contains(false)) {
      userAnswers.get(BusinessNameId).nonEmpty && userAnswers.get(PartnershipRegisteredAddressId).nonEmpty
    } else {
      userAnswers.get(BusinessNameId).nonEmpty && userAnswers.get(BusinessUTRId).nonEmpty
    }
  }

  private def loadCyaPage(mode: Mode)(implicit request: DataRequest[AnyContent]): Result = {
    val partnershipDetails = AnswerSection(None, Seq(
      BusinessNameId.row(None),
      BusinessUTRId.row(None),
      HasPAYEId.row(Some(Link(routes.HasPartnershipPAYEController.onPageLoad(CheckMode).url))),
      EnterPAYEId.row(Some(Link(routes.PartnershipEnterPAYEController.onPageLoad(CheckMode).url))),
      HasVATId.row(Some(Link(routes.HasPartnershipVATController.onPageLoad(CheckMode).url))),
      EnterVATId.row(Some(Link(routes.PartnershipEnterVATController.onPageLoad(CheckMode).url))),
      PartnershipContactAddressId.row(Some(Link(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(CheckMode).url))),
      PartnershipAddressYearsId.row(Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url))),
      PartnershipPreviousAddressId.row(Some(Link(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url))),
      PartnershipEmailId.row(Some(Link(routes.PartnershipEmailController.onPageLoad(CheckMode).url))),
      PartnershipPhoneId.row(Some(Link(routes.PartnershipPhoneController.onPageLoad(CheckMode).url)))
    ).flatten
    )

    Ok(view(
      Seq(partnershipDetails),
      controllers.register.partnership.routes.CheckYourAnswersController.onSubmit(),
      None,
      mode,
      request.userAnswers.isPartnershipDetailsComplete
    ))
  }
}
