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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.{ContactDetailsController, Retrievals}
import forms.ContactDetailsFormProvider
import identifiers.register.partnership.partners.{PartnerContactDetailsId, PartnerDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{ContactDetailsViewModel, Message}

class PartnerContactDetailsController @Inject()(
                                                 val appConfig: FrontendAppConfig,
                                                 val cacheConnector: UserAnswersCacheConnector,
                                                 @PartnershipPartner val navigator: Navigator,
                                                 val messagesApi: MessagesApi,
                                                 override val allowAccess: AllowAccessActionProvider,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: ContactDetailsFormProvider
                                               ) extends ContactDetailsController with Retrievals {

  def viewModel(mode: Mode, index: Index) = Retrieval {
    implicit request =>
      PartnerDetailsId(index).retrieve.right.map { details =>
        ContactDetailsViewModel(
          routes.PartnerContactDetailsController.onSubmit(mode, index),
          Message("partnership.partner.contactDetails.title"),
          Message("partnership.partner.contactDetails.heading"),
          None,
          Some(details.fullName)
        )
      }
  }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).retrieve.right.map(vm => get(PartnerContactDetailsId(index), formProvider(), vm))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).retrieve.right.map(vm => post(PartnerContactDetailsId(index), mode, formProvider(), vm))
  }

}
