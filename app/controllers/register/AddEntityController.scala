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
import models.{Mode, NormalMode}
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsResultException
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import viewmodels.EntityViewModel
import views.html.register.addEntity

import scala.concurrent.Future

trait AddEntityController extends FrontendController with Retrievals with I18nSupport {
  implicit val ec = play.api.libs.concurrent.Execution.defaultContext
  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def get(id: TypedIdentifier[Boolean], form: Form[Boolean], viewmodel: EntityViewModel, mode: Mode)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    Future.successful(Ok(addEntity(appConfig, form, viewmodel, mode)))
  }

  protected def post(
                      id: TypedIdentifier[Boolean],
                      form: Form[Boolean],
                      viewmodel: EntityViewModel,
                      mode: Mode
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    if (viewmodel.entities.isEmpty || viewmodel.entities.lengthCompare(viewmodel.maxLimit) >= 0) {
      Future.successful(Redirect(navigator.nextPage(id, NormalMode, request.userAnswers)))
    }
    else {
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(addEntity(appConfig, formWithErrors, viewmodel, mode))),
        value => {
          request.userAnswers.set(id)(value).fold(
            errors => {
              Logger.error("Unable to set user answer", JsResultException(errors))
              Future.successful(InternalServerError)
            },
            userAnswers => Future.successful(Redirect(navigator.nextPage(id, mode, userAnswers)))
          )
        }
      )
    }
  }

}
