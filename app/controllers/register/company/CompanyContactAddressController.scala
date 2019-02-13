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
import identifiers.register.company._
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class CompanyContactAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                override val cacheConnector: UserAnswersCacheConnector,
                                                @RegisterCompany override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressFormProvider,
                                                val countryOptions: CountryOptions,
                                                val auditService: AuditService) extends ManualAddressController {

  override protected val form: Form[Address] = formProvider("error.country.invalid")

  private def addressViewModel(mode: Mode) = Retrieval(
    implicit request =>
      BusinessDetailsId.retrieve.right.map { businessDetails =>
        ManualAddressViewModel(
          routes.CompanyContactAddressController.onSubmit(mode),
          countryOptions.options,
          Message("companyContactAddress.title"),
          Message("companyContactAddress.heading", businessDetails.companyName),
          None,
          Some(Message("companyContactAddress.lede", businessDetails.companyName))
        )
      }
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      addressViewModel(mode).retrieve.right.map(vm =>
        get(CompanyContactAddressId, CompanyContactAddressListId, vm))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      addressViewModel(mode).retrieve.right.map(vm =>
        post(CompanyContactAddressId, CompanyContactAddressListId, vm, mode, "Company Contact Address",
          CompanyContactAddressPostCodeLookupId))
  }

}
