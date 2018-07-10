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

package controllers.register.adviser

import config.FrontendAppConfig
import controllers.actions._
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, CheckYourAnswersId}
import javax.inject.Inject
import models.{CheckMode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Adviser
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            @Adviser navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            implicit val countryOptions: CountryOptions
                                          ) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val details = AdviserDetailsId.row(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
      val address = AdviserAddressId.row(routes.AdviserAddressController.onPageLoad(CheckMode).url)
      val sections = Seq(AnswerSection(None, details ++ address))
      Ok(check_your_answers(appConfig, sections, Some("common.adviser.secondary.heading"), routes.CheckYourAnswersController.onSubmit()))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
  }
}
