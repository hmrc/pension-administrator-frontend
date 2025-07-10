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
import views.html.reason

import scala.concurrent.{ExecutionContext, Future}

trait ReasonController extends FrontendBaseController with Retrievals with I18nSupport {

  protected implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected val dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def view: reason

  def get(id: TypedIdentifier[String], viewmodel: CommonFormWithHintViewModel, form: Form[String])
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id).fold(form)(form.fill)
    Future.successful(Ok(view(preparedForm, viewmodel)))
  }

  def post(id: TypedIdentifier[String], mode: Mode, viewmodel: CommonFormWithHintViewModel, form: Form[String])
          (implicit request: DataRequest[AnyContent]): Future[Result] =
    form.bindFromRequest().fold(
      (formWithErrors: Form[?]) =>
        Future.successful(BadRequest(view(formWithErrors, viewmodel))),
      reason => {
        dataCacheConnector.save(id, reason).map { cacheMap =>
          Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
        }
      }
    )
}
