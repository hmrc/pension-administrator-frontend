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
import controllers.Retrievals
import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{BusinessDetails, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.OrganisationNameViewModel
import views.html.organisationName

import scala.concurrent.Future

trait OrganisationNameController extends FrontendController with Retrievals with I18nSupport with NameCleansing {

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def formModel: BusinessDetailsFormModel

  private lazy val form = new BusinessDetailsFormProvider(isUK=false)(formModel)

  protected def get(id: TypedIdentifier[BusinessDetails], viewmodel: OrganisationNameViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(organisationName(appConfig, filledForm, viewmodel)))
  }

  protected def post(
                      id: TypedIdentifier[BusinessDetails],
                      viewmodel: OrganisationNameViewModel
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    cleanseAndBindOrRedirect(request.body.asFormUrlEncoded, "companyName", form) match {
      case Left(futureResult) => futureResult
      case Right(f) => f.fold(
        formWithErrors =>
          Future.successful(BadRequest(organisationName(appConfig, formWithErrors, viewmodel))),
        companyName =>
          cacheConnector.save(request.externalId, id, companyName).map {
            answers =>
              Redirect(navigator.nextPage(id, NormalMode, UserAnswers(answers)))
          }
      )
    }
  }

}
