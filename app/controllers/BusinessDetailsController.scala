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
import controllers.register.NameCleansing
import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{BusinessDetails, NormalMode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.BusinessDetailsViewModel
import views.html.businessDetails

import scala.concurrent.Future

trait BusinessDetailsController extends FrontendController with I18nSupport with NameCleansing {

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def formModel: BusinessDetailsFormModel

  protected def viewModel: BusinessDetailsViewModel

  private lazy val form = new BusinessDetailsFormProvider(isUK = true)(formModel)



  def get[I <: TypedIdentifier[BusinessDetails]](id: I)(implicit request: DataRequest[AnyContent]): Result = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }

    Ok(businessDetails(appConfig, preparedForm, viewModel))

  }

  def post[I <: TypedIdentifier[BusinessDetails]](id: I)(implicit request: DataRequest[AnyContent]): Future[Result] = {

    cleanseAndBindOrRedirect(request.body.asFormUrlEncoded, "companyName", form) match {
      case Left(futureResult) => futureResult
      case Right(f) => f.fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(businessDetails(appConfig, formWithErrors, viewModel))),
        value =>
          dataCacheConnector.save(request.externalId, id, value).map(cacheMap =>
            Redirect(navigator.nextPage(id, NormalMode, UserAnswers(cacheMap))))
      )
    }
  }
}
