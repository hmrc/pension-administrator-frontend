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

package controllers.register.administratorPartnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.IsRegisteredNameController
import forms.register.IsRegisteredNameFormProvider
import identifiers.register.BusinessNameId
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipV2
import viewmodels.{CommonFormViewModel, Message}
import views.html.register.isRegisteredName

import scala.concurrent.ExecutionContext

class PartnershipIsRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                      override val cacheConnector: UserAnswersCacheConnector,
                                                      @PartnershipV2 val navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      override val allowAccess: AllowAccessActionProvider,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: IsRegisteredNameFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      val view: isRegisteredName
                                                     )(implicit val executionContext: ExecutionContext) extends IsRegisteredNameController with Retrievals {

  val form: Form[Boolean] = formProvider("isRegisteredName.partnership.error")

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel.retrieve.map(get(_))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel.retrieve.map(post(_))
  }

  def viewmodel: Retrieval[CommonFormViewModel] = Retrieval {
    implicit request =>
      BusinessNameId.retrieve.map {
        name =>
          CommonFormViewModel(
            NormalMode,
            routes.PartnershipIsRegisteredNameController.onSubmit,
            Message("isRegisteredName.partnership.title", name),
            Message("isRegisteredName.partnership.heading", name)
          )
      }
  }


}
