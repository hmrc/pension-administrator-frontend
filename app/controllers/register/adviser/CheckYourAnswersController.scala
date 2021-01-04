/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.adviser

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.adviser._
import javax.inject.Inject
import models.Mode
import models.Mode._
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Navigator
import utils.annotations.Adviser
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           @Adviser navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           dataCompletion: DataCompletion,
                                           implicit val countryOptions: CountryOptions,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: check_your_answers
                                          )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveAdviserName(mode) { _ =>
        Future.successful(cyaPage(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val isDataComplete = dataCompletion.isAdviserComplete(request.userAnswers, mode)
      if (isDataComplete) {
        Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
      } else {
        cyaPage(mode)
      }
  }

  private def cyaPage(mode: Mode)(implicit request: DataRequest[AnyContent]): Result = {
    val adviserName = AdviserNameId.row(Some(Link(routes.AdviserNameController.onPageLoad(checkMode(mode)).url)))
    val address = AdviserAddressId.row(Some(Link(routes.AdviserAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url)))
    val details = AdviserEmailId.row(Some(Link(routes.AdviserEmailController.onPageLoad(checkMode(mode)).url))) ++
      AdviserPhoneId.row(Some(Link(routes.AdviserPhoneController.onPageLoad(checkMode(mode)).url)))

    val sections = Seq(AnswerSection(None, adviserName ++ address ++ details))

    Ok(view(sections, routes.CheckYourAnswersController.onSubmit(mode),
      psaName(), mode, dataCompletion.isAdviserComplete(request.userAnswers, mode)))
  }
}
