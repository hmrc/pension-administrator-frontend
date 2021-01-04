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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.EmailAddressController
import forms.EmailFormProvider
import identifiers.register.individual.IndividualEmailId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Individual
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

import scala.concurrent.ExecutionContext

class IndividualEmailController @Inject()(@Individual val navigator: Navigator,
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

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(IndividualEmailId, form, viewModel(mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(IndividualEmailId, mode, form, viewModel(mode))
  }

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.IndividualEmailController.onSubmit(mode),
      title = Message("individual.email.title"),
      heading = Message("individual.email.title"),
      mode = mode,
      entityName = Message("common.you")
    )
}
