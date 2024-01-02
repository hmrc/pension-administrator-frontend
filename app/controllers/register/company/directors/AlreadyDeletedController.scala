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

package controllers.register.company.directors

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company.directors.DirectorNameId
import javax.inject.Inject
import models.{Index, Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

import scala.concurrent.Future

class AlreadyDeletedController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          val allowAccess: AllowAccessActionProvider,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: alreadyDeleted) extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      DirectorNameId(index).retrieve.map { directorDetails =>
        Future.successful(Ok(view(viewmodel(directorDetails.fullName))))
      }
  }

  private def viewmodel(directorName: String) = AlreadyDeletedViewModel(
    Message("alreadyDeleted.director.title"),
    directorName,
    controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode)
  )

}
