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

package controllers.register.individual

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.individual.routes._
import identifiers.register.individual._
import models.Mode
import models.Mode.checkMode
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Individual
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.{Enumerable, Navigator}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            dataCompletion: DataCompletion,
                                            @Individual navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            implicit val countryOptions: CountryOptions,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: check_your_answers
                                          )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with
  Retrievals with I18nSupport with Enumerable.Implicits {

  import CheckYourAnswersController._

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val ua = request.userAnswers
      (ua.get(IndividualDetailsId), ua.get(IndividualAddressId)) match {
        case (Some(_), Some(_)) =>
          loadCyaPage(mode)
        case _ =>
          Redirect(controllers.register.routes.RegisterAsBusinessController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val isDataComplete = dataCompletion.isIndividualComplete(request.userAnswers, mode)
      if (isDataComplete) {
        Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
      } else {
        loadCyaPage(mode)
      }
  }

  private def loadCyaPage(mode: Mode)(implicit request: DataRequest[AnyContent]): Result = {
    val individualDetails = AnswerSection(None, Seq(
      IndividualDetailsId.row(None),
      IndividualDateOfBirthId.row(Some(Link(IndividualDateOfBirthController.onPageLoad(checkMode(mode)).url))),
      IndividualAddressId.row(None),
      IndividualSameContactAddressId.row(Some(Link(IndividualSameContactAddressController.onPageLoad(checkMode(mode)).url))),
      IndividualContactAddressId.row(Some(Link(IndividualContactAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url))),
      IndividualAddressYearsId.row(Some(Link(IndividualAddressYearsController.onPageLoad(checkMode(mode)).url))),
      IndividualPreviousAddressId.row(Some(Link(
        controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url))),
      IndividualEmailId.row(Some(Link(IndividualEmailController.onPageLoad(checkMode(mode)).url))),
      IndividualPhoneId.row(Some(Link(IndividualPhoneController.onPageLoad(checkMode(mode)).url)))
    ).flatten)
    val sections = Seq(individualDetails)
    Ok(view(sections, postUrl, None, mode, dataCompletion.isIndividualComplete(request.userAnswers, mode)))
  }
}

object CheckYourAnswersController {
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
}
