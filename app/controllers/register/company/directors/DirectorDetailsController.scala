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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.PersonDetailsController
import controllers.actions._
import identifiers.register.company.directors.DirectorDetailsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{Message, PersonDetailsViewModel}

class DirectorDetailsController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val dataCacheConnector: UserAnswersCacheConnector,
                                           @CompanyDirector val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction
                                         ) extends PersonDetailsController {

  private[directors] def viewModel(mode: Mode, index: Index) =
    PersonDetailsViewModel(
      title = "directorDetails.title",
      heading = Message("directorDetails.title"),
      postCall = routes.DirectorDetailsController.onSubmit(mode, index)
    )

  private[directors] def id(index: Index): DirectorDetailsId =
    DirectorDetailsId(index)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      get(id(index), viewModel(mode, index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(id(index), viewModel(mode, index), mode)
  }

}
