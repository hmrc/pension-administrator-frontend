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

package controllers.register.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.PayeController
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.PayeFormProvider
import identifiers.register.partnership.PartnershipPayeId
import models.{Mode, Paye}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{Message, PayeViewModel}

class PartnershipPayeController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           override val cacheConnector: UserAnswersCacheConnector,
                                           @Partnership val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: PayeFormProvider
                                         ) extends PayeController with I18nSupport {

  protected val form: Form[Paye] = formProvider()

  def viewmodel(mode: Mode) =
    PayeViewModel(
      postCall = routes.PartnershipPayeController.onSubmit(mode),
      title = Message("partnershipPaye.title"),
      heading = Message("partnershipPaye.heading"),
      hint = Some("common.paye.hint"),
      subHeading = None
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(
        PartnershipPayeId,
        form,
        viewmodel(mode)
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(
        PartnershipPayeId,
        mode,
        form,
        viewmodel(mode)
      )
  }
}
