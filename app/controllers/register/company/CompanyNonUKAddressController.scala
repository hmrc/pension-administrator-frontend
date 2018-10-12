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

package controllers.register.company

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.{ManualAddressController, NonUKAddressController}
import forms.AddressFormProvider
import forms.address.NonUKAddressFormProvider
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId}
import identifiers.register.company.CompanyRegisteredAddressId
import javax.inject.Inject
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request}
import play.twirl.api.HtmlFormat
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

class CompanyNonUKAddressController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val dataCacheConnector: UserAnswersCacheConnector,
                                               @RegisterCompany override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: NonUKAddressFormProvider,
                                               val countryOptions: CountryOptions
                                             ) extends NonUKAddressController {

  protected val form: Form[Address] = formProvider()

  protected override def createView(appConfig: FrontendAppConfig, preparedForm: Form[_], viewModel: ManualAddressViewModel)(
    implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable = () =>
    nonukAddress(appConfig, preparedForm, viewModel)(request, messages)

  private def addressViewModel(mode: Mode) = ManualAddressViewModel(
    routes.CompanyNonUKAddressController.onSubmit(mode),
    countryOptions.options,
    Message("nonUKRegisteredAddress.title"),
    Message("nonUKRegisteredAddress.heading"),
    None,
    Some(Message("nonUKRegisteredAddress.hinText"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(CompanyRegisteredAddressId, addressViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyRegisteredAddressId, addressViewModel(mode), mode)
  }
}
