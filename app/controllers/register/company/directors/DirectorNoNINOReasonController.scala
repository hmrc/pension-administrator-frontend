/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.company.directors.{DirectorNameId, DirectorNoNINOReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.reason

import scala.concurrent.ExecutionContext

class DirectorNoNINOReasonController @Inject()(@CompanyDirector val navigator: Navigator,
                                               val appConfig: FrontendAppConfig,
                                               val dataCacheConnector: UserAnswersCacheConnector,
                                               authenticate: AuthAction,
                                               val allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: ReasonFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: reason
                                              )(implicit val executionContext: ExecutionContext) extends ReasonController {

  private def form(directorName: String)
                  (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        get(DirectorNoNINOReasonId(index), viewModel(mode, index, directorName), form(directorName))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val directorName = entityName(index)
      post(DirectorNoNINOReasonId(index), mode, viewModel(mode, index, directorName), form(directorName))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorNoNINOReasonController.onSubmit(mode, index),
      title = Message("whyNoNINO.heading", Message("theDirector")),
      heading = Message("whyNoNINO.heading", directorName),
      mode = mode,
      entityName = directorName
    )
}
