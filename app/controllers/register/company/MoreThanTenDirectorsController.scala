/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{MoreThanTenController, Retrievals}
import identifiers.register.company.MoreThanTenDirectorsId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

import scala.concurrent.ExecutionContext

class MoreThanTenDirectorsController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               @CompanyDirector val navigator: Navigator,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: moreThanTen
                                              )(implicit val executionContext: ExecutionContext) extends MoreThanTenController with Retrievals {

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]): MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenDirectors.title",
      heading = "moreThanTenDirectors.heading",
      hint = "moreThanTenDirectors.hint",
      postCall = routes.MoreThanTenDirectorsController.onSubmit(mode),
      id = MoreThanTenDirectorsId,
      psaName = psaName(),
      errorKey = "moreThanTenDirectors.error.required"
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      get(viewModel(mode), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(viewModel(mode), mode)
  }
}
