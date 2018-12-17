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

package controllers.register

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.AreYouInUKFormProvider
import identifiers.register.AreYouInUKId
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.AreYouInUKViewModel
import views.html.register.areYouInUK

import scala.concurrent.Future

trait AreYouInUKController extends FrontendController with I18nSupport {
  implicit val ec = play.api.libs.concurrent.Execution.defaultContext
  protected val appConfig: FrontendAppConfig
  protected val dataCacheConnector: UserAnswersCacheConnector
  protected val navigator: Navigator
  protected val authenticate: AuthAction
  protected val getData: DataRetrievalAction
  protected val requireData: DataRequiredAction
  protected val formProvider: AreYouInUKFormProvider

  protected val form = formProvider()

  protected def viewmodel(mode: Mode) : AreYouInUKViewModel

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AreYouInUKId).fold(form)(v=>form.fill(v))
      Ok(areYouInUK(appConfig, preparedForm, viewmodel(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(areYouInUK(appConfig, formWithErrors, viewmodel(mode)))),
        value => {
          dataCacheConnector.save(request.externalId, AreYouInUKId, value).map(cacheMap =>
            Redirect(navigator.nextPage(AreYouInUKId, mode, UserAnswers(cacheMap))))
        })
  }
}
