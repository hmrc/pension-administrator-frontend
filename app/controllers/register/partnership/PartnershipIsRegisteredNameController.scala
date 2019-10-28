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

package controllers.register.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.IsRegisteredNameController
import forms.register.IsRegisteredNameFormProvider
import identifiers.register.BusinessNameId
import models.NormalMode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{CommonFormViewModel, Message}

class PartnershipIsRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      override val cacheConnector: UserAnswersCacheConnector,
                                                      @Partnership override val navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      override val allowAccess: AllowAccessActionProvider,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: IsRegisteredNameFormProvider) extends IsRegisteredNameController with Retrievals {

  val form: Form[Boolean] = formProvider("isRegisteredName.partnership.error")

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel.retrieve.right.map(get(_))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel.retrieve.right.map(post(_))
  }

  def viewmodel: Retrieval[CommonFormViewModel] = Retrieval {
    implicit request =>
    BusinessNameId.retrieve.right.map {
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
