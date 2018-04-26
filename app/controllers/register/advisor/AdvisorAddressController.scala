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

package controllers.register.advisor

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.MessagesApi
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.advisor.{AdvisorAddressId, AdvisorAddressListId}
import models.{Address, Mode}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.Adviser
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class AdvisorAddressController @Inject()(
                                          override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val dataCacheConnector: DataCacheConnector,
                                          @Adviser override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions
                                        ) extends ManualAddressController {

  protected val form: Form[Address] = formProvider()

  private def addressViewModel(mode: Mode) = ManualAddressViewModel(
    routes.AdvisorAddressController.onSubmit(mode),
    countryOptions.options,
    Message("common.advisor.address.title"),
    Message("common.advisor.address.heading"),
    Some(Message("common.advisor.secondary.heading"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(AdvisorAddressId, addressViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdvisorAddressId, addressViewModel(mode), mode)
  }
}
