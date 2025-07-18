/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import forms.MoreThanTenFormProvider
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

import scala.concurrent.Future

trait MoreThanTenController extends FrontendBaseController with I18nSupport with Variations {

  protected def navigator: Navigator

  private def form(errorKey: String): Form[Boolean] = new MoreThanTenFormProvider()(errorKey)

  protected def view: moreThanTen

  def get(viewModel: MoreThanTenViewModel, mode: Mode)(implicit request: DataRequest[AnyContent]): Result = {
    val preparedForm = request.userAnswers.get(viewModel.id) match {
      case None => form(viewModel.errorKey)
      case Some(value) => form(viewModel.errorKey).fill(value)
    }
    Ok(view(preparedForm, viewModel, mode))
  }

  def post(viewModel: MoreThanTenViewModel, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form(viewModel.errorKey).bindFromRequest().fold(
      (formWithErrors: Form[?]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, mode))),
      value => {
        val hasAnswerChanged = request.userAnswers.get(viewModel.id) match {
          case None => true
          case Some(false) => value
          case Some(true) => !value
        }

        if (hasAnswerChanged) {
          cacheConnector.save(viewModel.id, value).flatMap(cacheMap =>
            saveChangeFlag(mode, viewModel.id).map(_ =>
              Redirect(navigator.nextPage(viewModel.id, mode, UserAnswers(cacheMap))))
          )
        } else {
          cacheConnector.save(viewModel.id, value).map(cacheMap =>
            Redirect(navigator.nextPage(viewModel.id, mode, UserAnswers(cacheMap))))
        }
      }
    )
  }

}
