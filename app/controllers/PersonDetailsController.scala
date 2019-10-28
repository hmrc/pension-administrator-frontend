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
import connectors.UserAnswersCacheConnector
import controllers.actions.AllowAccessActionProvider
import forms.PersonDetailsFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, PersonDetails}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.PersonDetailsViewModel
import views.html.personDetails

import scala.concurrent.Future

trait PersonDetailsController extends FrontendController with I18nSupport with Variations {

  protected val allowAccess: AllowAccessActionProvider

  override implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  def appConfig: FrontendAppConfig

  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  private val form = new PersonDetailsFormProvider()()

  def get[I <: TypedIdentifier[PersonDetails]](
                                                id: I, viewModel:
  PersonDetailsViewModel, mode: Mode
                                              )(implicit request: DataRequest[AnyContent]): Result = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }

    Ok(personDetails(appConfig, preparedForm, viewModel, mode))

  }

  def post[I <: TypedIdentifier[PersonDetails]](
                                                 id: I,
                                                 viewModel: PersonDetailsViewModel,
                                                 mode: Mode
                                               )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Future.successful(BadRequest(personDetails(appConfig, formWithErrors, viewModel, mode))),
      value =>
        cacheConnector.save(request.externalId, id, value).flatMap { cacheMap =>
          setNewFlagPerson(id, mode, UserAnswers(cacheMap)).map { updatedUserAnswers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(updatedUserAnswers)))
          }
        }
    )
  }
}
