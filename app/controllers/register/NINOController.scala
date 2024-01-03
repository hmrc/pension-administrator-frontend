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

package controllers.register

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.{Retrievals, Variations}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, ReferenceValue}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.enterNINO

import scala.concurrent.{ExecutionContext, Future}

trait NINOController extends FrontendBaseController with Retrievals with I18nSupport with Variations {

  implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def view: enterNINO

  def get(id: TypedIdentifier[ReferenceValue], form: Form[ReferenceValue], viewmodel: CommonFormWithHintViewModel)
         (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(preparedForm, viewmodel)))
  }

  def post(id: TypedIdentifier[ReferenceValue], mode: Mode, form: Form[ReferenceValue], viewmodel: CommonFormWithHintViewModel)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(formWithErrors, viewmodel))),
      value =>
        cacheConnector.save(request.externalId, id, value.copy(isEditable = true)).flatMap(
          cacheMap =>
            saveChangeFlag(mode, id).map { _ =>
              Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
            }
        )
    )
  }
}
