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
import identifiers.TypedIdentifier
import models.NormalMode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.OrganisationNameViewModel
import views.html.organisationName

import scala.concurrent.{ExecutionContext, Future}

trait OrganisationNameController extends FrontendBaseController with Retrievals with I18nSupport with NameCleansing {

  implicit val executionContext: ExecutionContext

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  def form: Form[String]

  protected val view: organisationName

  protected def get(id: TypedIdentifier[String], viewmodel: OrganisationNameViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(filledForm, viewmodel)))
  }

  protected def post(id: TypedIdentifier[String], viewmodel: OrganisationNameViewModel)
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {

    cleanseAndBindOrRedirect(request.body.asFormUrlEncoded, "value", form) match {
      case Left(futureResult) => futureResult
      case Right(f) => f.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, viewmodel))),
        companyName =>
          cacheConnector.save(id, companyName).map {
            answers =>
              Redirect(navigator.nextPage(id, NormalMode, UserAnswers(answers)))
          }
      )
    }
  }

}
