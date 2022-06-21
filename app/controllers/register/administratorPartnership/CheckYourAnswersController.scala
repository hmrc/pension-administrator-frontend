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

package controllers.register.administratorPartnership

import controllers.Retrievals
import controllers.actions._
import identifiers.register._
import identifiers.register.partnership._
import models.requests.DataRequest
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.PartnershipV2
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.{Navigator, UserAnswers}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class CheckYourAnswersController @Inject()(
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            dataCompletion: DataCompletion,
                                            @PartnershipV2 val navigator: Navigator,
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
      val isDataComplete = dataCompletion.isPartnershipDetailsComplete(request.userAnswers)
      if (isDataComplete) {
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      } else {
        loadCyaPage(mode)
      }
  }

  private def isMandatoryDataPresent(userAnswers: UserAnswers): Boolean = {
    val isRegInfoComplete = userAnswers.get(BusinessNameId).nonEmpty && userAnswers.get(RegistrationInfoId).nonEmpty
    if (userAnswers.get(AreYouInUKId).contains(false)) {
      isRegInfoComplete && userAnswers.get(PartnershipRegisteredAddressId).nonEmpty
    } else {
      isRegInfoComplete && userAnswers.get(BusinessUTRId).nonEmpty
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
      dataCompletion.isPartnershipDetailsComplete(request.userAnswers)
    ))
  }
}
