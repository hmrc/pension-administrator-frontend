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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import identifiers.register.company.DirectorDetailsId
import models.{Index, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import play.api.mvc.{Action, AnyContent}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import views.html.register.company.confirmDeleteDirector

import scala.concurrent.Future

class ConfirmDeleteDirectorController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                sessionRepository: SessionRepository) extends FrontendController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Ok(confirmDeleteDirector(appConfig, index))
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val removal = request.userAnswers.remove(DirectorDetailsId(index))

      removal match {
        case JsSuccess(UserAnswers(updatedJson), _) =>
          sessionRepository().upsert(request.externalId, updatedJson)
            .map(_ => Redirect(routes.AddCompanyDirectorsController.onPageLoad(NormalMode)))
        case JsError(errors) =>
          Future.failed(JsResultException(errors))
      }

  }
}
