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
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.partnership.PartnershipContactAddressListId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.address.AddressListViewModel

class PartnershipAddressListController @Inject()(
                                                val cacheConnector: DataCacheConnector,
                                                @Partnership val navigator: Navigator,
                                                val appConfig: FrontendAppConfig,
                                                val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressListFormProvider
                                                ) extends AddressListController {

  def viewModel(mode: Mode, index: Index) = AddressListViewModel(
    routes.PartnershipAddressListController.onSubmit(mode, index),
    routes.PartnershipAddressListController.onSubmit(mode, index),
    Seq.empty
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode,index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(viewModel(mode, index), PartnershipContactAddressListId(index), ???, mode)
  }

}
