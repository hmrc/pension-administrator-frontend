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

package controllers.register

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.enterPAYE

import scala.concurrent.{ExecutionContext, Future}

trait EnterPAYEController extends FrontendBaseController with I18nSupport {

  implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def view: enterPAYE

  def get(id: TypedIdentifier[String], form: Form[String], viewModel: CommonFormWithHintViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id).map(form.fill).getOrElse(form)
    Future.successful(Ok(view(preparedForm, viewModel)))
  }

  def post(id: TypedIdentifier[String], mode: Mode, form: Form[String], viewModel: CommonFormWithHintViewModel)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel))),
      value =>
        cacheConnector.save(request.externalId, id, value).map(
          cacheMap =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
        )
    )
  }
}
