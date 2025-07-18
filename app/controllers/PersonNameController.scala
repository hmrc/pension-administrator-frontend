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

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.AllowAccessActionProvider
import forms.PersonNameFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, PersonName}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.personName

import scala.concurrent.Future

trait PersonNameController extends FrontendBaseController with Variations {

  protected val allowAccess: AllowAccessActionProvider


  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  protected def view: personName

  private val form = new PersonNameFormProvider()()

  def get[I <: TypedIdentifier[PersonName]](id: I, viewModel: CommonFormWithHintViewModel, mode: Mode)
                                           (implicit request: DataRequest[AnyContent], messages: Messages): Result = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, viewModel, mode))

  }

  def post[I <: TypedIdentifier[PersonName]](id: I, viewModel: CommonFormWithHintViewModel, mode: Mode)
                                            (implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[?]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, mode))),
      value =>
        cacheConnector.save(id, value).flatMap { cacheMap =>
          setNewFlag(id, mode, UserAnswers(cacheMap)).map { updatedUserAnswers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(updatedUserAnswers)))
          }
        }
    )
  }
}
