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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.partnership.partners.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostCodeLookupId}
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.PartnershipPartner
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PartnerAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val dataCacheConnector: UserAnswersCacheConnector,
                                         @PartnershipPartner override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddressFormProvider,
                                         countryOptions: CountryOptions,
                                         val auditService: AuditService) extends ManualAddressController with Retrievals {

  override protected val form: Form[Address] = formProvider()

  private def addressViewModel(mode: Mode, index: Index, partnerName: String) = ManualAddressViewModel(
    routes.PartnerAddressController.onSubmit(mode, index),
    countryOptions.options,
    Message("partnerAddress.title"),
    Message("partnerAddress.heading"),
    Some(Message(partnerName))
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnerName(index) {
        partnerName =>
          get(PartnerAddressId(index), PartnerAddressListId(index), addressViewModel(mode, index, partnerName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrievePartnerName(index) {
        partnerName =>
          val vm = addressViewModel(mode, index, partnerName)
          post(PartnerAddressId(index), PartnerAddressListId(index), vm, mode, context(vm),
            PartnerAddressPostCodeLookupId(index))
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Partnership Partner Address: $name"
      case _ => "Partnership Partner Address"
    }
  }

}
