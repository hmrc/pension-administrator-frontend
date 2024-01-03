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

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.hasReferenceNumber

import scala.concurrent.{ExecutionContext, Future}

trait HasReferenceNumberController extends FrontendBaseController with Retrievals with I18nSupport {

  implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected val dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def view: hasReferenceNumber

  def get(id: TypedIdentifier[Boolean], form: Form[Boolean], viewModel: CommonFormWithHintViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)
    Future.successful(Ok(view(preparedForm, viewModel)))
  }

  def post(id: TypedIdentifier[Boolean], mode: Mode, form: Form[Boolean], viewModel: CommonFormWithHintViewModel)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel))),
      value => {
        dataCacheConnector.save(request.externalId, id, value).map{cacheMap =>
          Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))}
      }
    )
  }
}
