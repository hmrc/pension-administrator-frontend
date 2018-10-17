/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.CompanyNameFormProvider
import identifiers.TypedIdentifier
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.CompanyNameViewModel
import views.html.companyName

import scala.concurrent.Future

trait CompanyNameController extends FrontendController with Retrievals with I18nSupport {

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected val form: Form[String] = new CompanyNameFormProvider().apply()

  protected def get(id: TypedIdentifier[String], viewmodel: CompanyNameViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(companyName(appConfig, filledForm, viewmodel)))
  }

  protected def post(
                      id: TypedIdentifier[String],
                      mode: Mode,
                      viewmodel: CompanyNameViewModel
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(companyName(appConfig, formWithErrors, viewmodel))),
      companyName =>
        cacheConnector.save(request.externalId, id, companyName).map {
          answers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(answers)))
        }
    )
  }
}