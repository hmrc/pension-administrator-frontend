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

package controllers.register.company

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.company.{CompanyAddressListId, CompanyPreviousAddressId, CompanyPreviousAddressPostCodeLookupId}
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class CompanyPreviousAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 override val dataCacheConnector: UserAnswersCacheConnector,
                                                 @RegisterCompany override val navigator: Navigator,
                                                 override val allowAccess: AllowAccessActionProvider,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: AddressFormProvider,
                                                 val countryOptions: CountryOptions,
                                                 val auditService: AuditService) extends ManualAddressController {

  override protected val form: Form[Address] = formProvider("error.country.invalid")

  private def addressViewModel(mode: Mode) = ManualAddressViewModel(
    routes.CompanyPreviousAddressController.onSubmit(mode),
    countryOptions.options,
    Message("companyPreviousAddress.title"),
    Message("companyPreviousAddress.heading"),
    None
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(CompanyPreviousAddressId, CompanyAddressListId, addressViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyPreviousAddressId, CompanyAddressListId, addressViewModel(mode), mode, "Company Previous Address",
        CompanyPreviousAddressPostCodeLookupId)
  }

}
