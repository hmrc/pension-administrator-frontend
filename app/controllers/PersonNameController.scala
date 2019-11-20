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

package controllers

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.AllowAccessActionProvider
import forms.PersonNameFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, PersonName}
import play.api.data.Form
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.personName

import scala.concurrent.Future

trait PersonNameController extends FrontendBaseController with Variations {

  protected val allowAccess: AllowAccessActionProvider

  def appConfig: FrontendAppConfig

  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  protected def view: personName

  private val form = new PersonNameFormProvider()()

  def get[I <: TypedIdentifier[PersonName]](
                                             id: I, viewModel: CommonFormWithHintViewModel,
                                             mode: Mode
                                              )(implicit request: DataRequest[AnyContent]): Result = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, viewModel, mode)(request, implicitly))

  }

  def post[I <: TypedIdentifier[PersonName]](
                                                 id: I,
                                                 viewModel: CommonFormWithHintViewModel,
                                                 mode: Mode
                                               )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, mode)(request, implicitly))),
      value =>
        cacheConnector.save(request.externalId, id, value).flatMap { cacheMap =>
          setNewFlag(id, mode, UserAnswers(cacheMap)).map { updatedUserAnswers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(updatedUserAnswers)))
          }
        }
    )
  }
}
