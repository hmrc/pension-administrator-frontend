/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions._
import controllers.register.EmailAddressController
import forms.EmailFormProvider
import identifiers.register.company.directors.{DirectorEmailId, DirectorNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

import scala.concurrent.ExecutionContext

class DirectorEmailController @Inject()(@CompanyDirector val navigator: Navigator,
                                        val appConfig: FrontendAppConfig,
                                        val cacheConnector: UserAnswersCacheConnector,
                                        authenticate: AuthAction,
                                        val allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EmailFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: email
                                       )(implicit val executionContext: ExecutionContext) extends EmailAddressController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(DirectorEmailId(index), form, viewModel(mode, index, entityName(index)))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(DirectorEmailId(index), mode, form, viewModel(mode, index, entityName(index)))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorEmailController.onSubmit(mode, index),
      title = Message("email.title", Message("theDirector")),
      heading = Message("email.title", directorName),
      mode = mode,
      entityName = directorName
    )
}
