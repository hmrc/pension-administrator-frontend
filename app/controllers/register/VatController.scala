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

package controllers.register

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, Vat}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.VatViewModel
import views.html.register.vat

import scala.concurrent.Future

trait VatController extends FrontendController with Retrievals with I18nSupport {

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  def get(id: TypedIdentifier[Vat], form: Form[Vat], viewmodel: VatViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(vat(appConfig, preparedForm, viewmodel)))
  }

  def post(id: TypedIdentifier[Vat], mode: Mode, form: Form[Vat], viewmodel: VatViewModel)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(vat(appConfig, formWithErrors, viewmodel))),
      value =>
        cacheConnector.save(request.externalId, id, value).map(cacheMap =>
          Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap))))
    )
  }
}
