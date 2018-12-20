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

package controllers.register.adviser

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId, AdviserAddressPostCodeLookupId}
import javax.inject.Inject
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Adviser
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class AdviserAddressController @Inject()(
                                          override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val dataCacheConnector: UserAnswersCacheConnector,
                                          @Adviser override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions,
                                          val auditService: AuditService
                                        ) extends ManualAddressController {

  protected val form: Form[Address] = formProvider()

  private def addressViewModel(mode: Mode) = ManualAddressViewModel(
    routes.AdviserAddressController.onSubmit(mode),
    countryOptions.options,
    Message("common.adviser.address.title"),
    Message("common.adviser.address.heading"),
    Some(Message("common.adviser.secondary.heading"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(AdviserAddressId, AdviserAddressListId, addressViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserAddressId, AdviserAddressListId, addressViewModel(mode), mode, "Adviser Address", AdviserAddressPostCodeLookupId)
  }

}
