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

package controllers.register.company.directors

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.company.directors.{CompanyDirectorAddressListId, DirectorAddressId}
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class DirectorAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val dataCacheConnector: UserAnswersCacheConnector,
                                          @CompanyDirector override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AddressFormProvider,
                                          countryOptions: CountryOptions,
                                          val auditService: AuditService) extends ManualAddressController with Retrievals {

  override protected val form: Form[Address] = formProvider()

  private def addressViewModel(mode: Mode, index: Index, directorName: String) = ManualAddressViewModel(
    routes.DirectorAddressController.onSubmit(mode, index),
    countryOptions.options,
    Message("directorAddress.title"),
    Message("directorAddress.heading"),
    Some(Message(directorName))
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) {
        directorName =>
          get(DirectorAddressId(index), CompanyDirectorAddressListId(index), addressViewModel(mode, index, directorName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) {
        directorName =>
          val vm = addressViewModel(mode, index, directorName)
          post(DirectorAddressId(index), CompanyDirectorAddressListId(index), vm, mode, context(vm))
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Company Director Address: $name"
      case _ => "Company Director Address"
    }
  }

}
