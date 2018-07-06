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

package controllers.register.company

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import identifiers.TypedIdentifier
import identifiers.register.company.AddCompanyDirectorsId
import models.requests.DataRequest
import models.{Mode, NormalMode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsResultException
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import viewmodels.{EntityViewModel, Person}
import views.html.register.addEntity
import scala.concurrent.Future

trait AddEntityController extends FrontendController with Retrievals with I18nSupport {

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: DataCacheConnector

  protected def navigator: Navigator

  protected def get(id: TypedIdentifier[Boolean], form: Form[Boolean], viewmodel: EntityViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    Future.successful(Ok(addEntity(appConfig, form, viewmodel, disableSubmission(viewmodel.entities))))
  }

  protected def post(
                      id: TypedIdentifier[Boolean],
                      form: Form[Boolean],
                      viewmodel: EntityViewModel
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    if (viewmodel.entities.isEmpty || viewmodel.entities.lengthCompare(viewmodel.maxLimit) >= 0) {
      Future.successful(Redirect(navigator.nextPage(id, NormalMode, request.userAnswers)))
    }
    else {
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(addEntity(appConfig, formWithErrors, viewmodel, disableSubmission(viewmodel.entities)))),
        value => {
          request.userAnswers.set(id)(value).fold(
            errors => {
              Logger.error("Unable to set user answer", JsResultException(errors))
              Future.successful(InternalServerError)
            },
            userAnswers => Future.successful(Redirect(navigator.nextPage(id, NormalMode, userAnswers)))
          )
        }
      )
    }
  }

  private def disableSubmission(entitiesWithFlag: Seq[Person])(implicit request: DataRequest[AnyContent]): Boolean =
    appConfig.completeFlagEnabled & entitiesWithFlag.foldLeft(true) { (_, person) =>
      !person.isComplete.fold(false)(isComplete => isComplete)
    }
}
