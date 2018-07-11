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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.partnership.{PartnershipContactAddressId, PartnershipContactAddressListId, PartnershipContactAddressPostCodeLookupId, PartnershipDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.address.AddressListViewModel

class PartnershipContactAddressListController @Inject()(
                                                val cacheConnector: DataCacheConnector,
                                                @Partnership val navigator: Navigator,
                                                val appConfig: FrontendAppConfig,
                                                val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressListFormProvider
                                                ) extends AddressListController with Retrievals {

  def viewModel(mode: Mode) = Retrieval { implicit request =>
    (PartnershipDetailsId and PartnershipContactAddressPostCodeLookupId).retrieve.right map { case details ~ addresses =>
      AddressListViewModel(
        routes.PartnershipContactAddressListController.onSubmit(mode),
        routes.PartnershipContactAddressController.onPageLoad(mode),
        addresses,
        subHeading = Some(details.name)
      )
    }
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map(get)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map { vm =>
        post(vm, PartnershipContactAddressListId, PartnershipContactAddressId, mode)
      }
  }

}
