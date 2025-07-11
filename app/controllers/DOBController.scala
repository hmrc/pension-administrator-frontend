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
import forms.DOBFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.dob

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

trait DOBController extends FrontendBaseController with I18nSupport with Variations {

  protected val allowAccess: AllowAccessActionProvider

  implicit val executionContext: ExecutionContext

  def cacheConnector: UserAnswersCacheConnector

  def navigator: Navigator

  val view: dob

  private val form = new DOBFormProvider()()

  def get[I <: TypedIdentifier[LocalDate]](id: I, viewModel: CommonFormWithHintViewModel)
                                          (implicit request: DataRequest[AnyContent]): Result = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, viewModel))

  }

  def post[I <: TypedIdentifier[LocalDate]](id: I, viewModel: CommonFormWithHintViewModel)
                                           (implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      (formWithErrors: Form[?]) =>
        Future.successful(BadRequest(view(formWithErrors, viewModel))),
      value =>
        cacheConnector.save(id, value).flatMap { cacheMap =>
          Future.successful(Redirect(navigator.nextPage(id, viewModel.mode, UserAnswers(cacheMap))))
        }
    )
  }
}
