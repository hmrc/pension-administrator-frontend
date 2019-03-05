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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.partnership.{PartnershipContactAddressId, PartnershipContactAddressListId, PartnershipContactAddressPostCodeLookupId, PartnershipDetailsId}
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PartnershipContactAddressController @Inject()(
                                                     val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val cacheConnector: UserAnswersCacheConnector,
                                                     @Partnership val navigator: Navigator,
                                                     override val allowAccess: AllowAccessActionProvider,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddressFormProvider,
                                                     val countryOptions: CountryOptions,
                                                     val auditService: AuditService
                                                   ) extends ManualAddressController with I18nSupport {

  protected val form: Form[Address] = formProvider("error.country.invalid")

  def viewmodel(mode: Mode, partnershipName: String) =
    ManualAddressViewModel(
      postCall = routes.PartnershipContactAddressController.onSubmit(mode),
      countryOptions = countryOptions.options,
      title = Message("partnership.contactAddress.title"),
      heading = Message("partnership.contactAddress.heading").withArgs(partnershipName),
      secondaryHeader = None,
      hint = Some(Message("partnership.contactAddress.hint").withArgs(partnershipName))
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnershipDetailsId.retrieve.right.map {
        details =>
          get(
            PartnershipContactAddressId,
            PartnershipContactAddressListId,
            viewmodel(mode, details.companyName),
            mode
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnershipDetailsId.retrieve.right.map {
        details =>
          post(
            PartnershipContactAddressId,
            PartnershipContactAddressListId,
            viewmodel(mode, details.companyName),
            mode,
            context = "Partnership contact address",
            PartnershipContactAddressPostCodeLookupId
          )
      }
  }
}
