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

package controllers

import config.FrontendAppConfig
import connectors.DataCacheConnector
import forms.MoreThanTenFormProvider
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

import scala.concurrent.Future

trait MoreThanTenController extends FrontendController with I18nSupport {

  protected def appConfig: FrontendAppConfig

  protected def dataCacheConnector: DataCacheConnector

  protected def navigator: Navigator

  private val form: Form[Boolean] = new MoreThanTenFormProvider()()

  def get(viewModel: MoreThanTenViewModel)(implicit request: DataRequest[AnyContent]): Result = {
    val preparedForm = request.userAnswers.get(viewModel.id) match {
      case None => form
      case Some(value) => form.fill(value)
    }
    Ok(moreThanTen(appConfig, preparedForm, viewModel))
  }

  def post(viewModel: MoreThanTenViewModel, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result]= {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(moreThanTen(appConfig, formWithErrors, viewModel))),
      value =>
        dataCacheConnector.save(request.externalId, viewModel.id, value).map(cacheMap =>
          Redirect(navigator.nextPage(viewModel.id, mode, UserAnswers(cacheMap))))
    )
  }

}
