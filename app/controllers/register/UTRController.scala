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
import controllers.Variations
import controllers.actions.AllowAccessActionProvider
import forms.UTRFormProvider
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.register.utr

import scala.concurrent.{ExecutionContext, Future}

trait UTRController extends FrontendController with I18nSupport with Variations {

  protected val allowAccess: AllowAccessActionProvider

  protected implicit def ec : ExecutionContext

  def appConfig: FrontendAppConfig

  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  private val form = new UTRFormProvider()()

  def get[I <: TypedIdentifier[String]](
                                                id: I, entity: String, href: Call
                                              )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).fold(form)(form.fill)
    Future.successful(Ok(utr(appConfig, preparedForm, entity, href)))

  }

  def post[I <: TypedIdentifier[String]](
                                                 id: I, entity: String, href: Call, mode: Mode
                                               )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(utr(appConfig, formWithErrors, entity, href))),
      value =>
        cacheConnector.save(request.externalId, id, value).flatMap { cacheMap =>
            Future.successful(Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap))))
          }
    )
  }
}