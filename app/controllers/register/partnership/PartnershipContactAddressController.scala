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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.partnership.{PartnershipContactAddressId, PartnershipContactAddressListId, PartnershipDetailsId}
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.AnyContent
import play.api.mvc.Action
import utils.Navigator
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PartnershipContactAddressController @Inject()(
                                                     val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val dataCacheConnector: DataCacheConnector,
                                                     @Partnership val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddressFormProvider,
                                                     val countryOptions: CountryOptions,
                                                     val auditService: AuditService
                                                   ) extends ManualAddressController with I18nSupport {

  protected val form: Form[Address] = formProvider("error.country.invalid")

  def viewmodel(mode: Mode, index: Index, partnershipName: String) =
    ManualAddressViewModel(
      postCall = routes.PartnershipContactAddressController.onSubmit(mode, index),
      countryOptions = countryOptions.options,
      title = Message("partnership.contactAddress.title"),
      heading = Message("partnership.contactAddress.heading").withArgs(partnershipName),
      secondaryHeader = Some("site.secondaryHeader"),
      hint = Some(Message("partnership.contactAddress.hint").withArgs(partnershipName))
    )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map {
        details =>
          get(
            PartnershipContactAddressId(index),
            PartnershipContactAddressListId(index),
            viewmodel(mode, index, details.name)
          )
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map {
        details =>
          post(
            PartnershipContactAddressId(index),
            PartnershipContactAddressListId(index),
            viewmodel(mode, index, details.name),
            mode,
            context = "Partnership contact address"
          )
      }
  }
}
