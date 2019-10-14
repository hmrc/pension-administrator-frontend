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
import identifiers.TypedIdentifier
import identifiers.register.IsRegisteredNameId
import models.register.BusinessType
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormViewModel, Message}
import views.html.register.isRegisteredName

import scala.concurrent.Future

trait IsRegisteredNameController extends FrontendController with I18nSupport with Variations {

  protected val allowAccess: AllowAccessActionProvider

  override implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  def appConfig: FrontendAppConfig

  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  def form: Form[Boolean]

  def get[I <: TypedIdentifier[Boolean]](viewmodel: CommonFormViewModel,
                                         id: I = IsRegisteredNameId
                                       )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).fold(form)(form.fill)
    Future.successful(Ok(isRegisteredName(appConfig, preparedForm, viewmodel)))

  }

  def post[I <: TypedIdentifier[Boolean]](viewmodel: CommonFormViewModel,
                                          id: I = IsRegisteredNameId
                                        )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(isRegisteredName(appConfig, formWithErrors, viewmodel))),
      value =>
        cacheConnector.save(request.externalId, id, value).flatMap { cacheMap =>
          Future.successful(Redirect(navigator.nextPage(id, viewmodel.mode, UserAnswers(cacheMap))))
        }
    )
  }

  def toString(businessType: BusinessType): String = Message(s"businessType.${businessType.toString}").toLowerCase()
}