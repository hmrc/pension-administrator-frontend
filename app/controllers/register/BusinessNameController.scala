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

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.{BusinessNameId, BusinessTypeId}
import models.NormalMode
import models.register.BusinessType
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import views.html.register.businessName

import scala.concurrent.{ExecutionContext, Future}

trait BusinessNameController extends FrontendBaseController with I18nSupport with Retrievals {

  implicit val executionContext: ExecutionContext

  def cacheConnector: UserAnswersCacheConnector
  def navigator: Navigator
  def authenticate: AuthAction
  def allowAccess: AllowAccessActionProvider
  def getData: DataRetrievalAction
  def requireData: DataRequiredAction
  def href: Call
  def form: Form[String]
  protected def view: businessName

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
  implicit request =>
  val prepareForm = request.userAnswers.get(BusinessNameId).fold(form)(form.fill)
    BusinessTypeId.retrieve.map { businessType =>
      Future.successful(Ok(view(prepareForm, toString(businessType), href)))
    }

}

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          BusinessTypeId.retrieve.map { businessType =>
            Future.successful(BadRequest(view(formWithErrors, toString(businessType), href)))
          },
        value =>
          cacheConnector.save(BusinessNameId, value).map(cacheMap =>
            Redirect(navigator.nextPage(BusinessNameId, NormalMode, UserAnswers(cacheMap))))
      )
  }

  def toString(businessType: BusinessType)(implicit messages: Messages): String =
    Message(s"businessType.${businessType.toString}").toLowerCase()

}
