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

package controllers

import config.FrontendAppConfig
import connectors.DataCacheConnector
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, Paye}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.PayeViewModel
import views.html.paye

import scala.concurrent.Future

trait PayeController extends FrontendController with Retrievals with I18nSupport {

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: DataCacheConnector

  protected def navigator: Navigator

  protected def get(id: TypedIdentifier[Paye], form: Form[Paye], viewmodel: PayeViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(paye(appConfig, filledForm, viewmodel)))
  }

  protected def post(
                      id: TypedIdentifier[Paye],
                      mode: Mode,
                      form: Form[Paye],
                      viewmodel: PayeViewModel
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(paye(appConfig, formWithErrors, viewmodel))),
      paye =>
        cacheConnector.save(request.externalId, id, paye).map {
          answers =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(answers)))
        }
    )
  }
}
