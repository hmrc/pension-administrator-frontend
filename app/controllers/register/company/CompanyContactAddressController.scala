/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, AuthAction, AllowAccessActionProvider, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company._
import models.requests.DataRequest
import models.{Mode, Address}
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import utils.Navigator
import utils.annotations.NoUpdateContactAddress
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class CompanyContactAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val cacheConnector: UserAnswersCacheConnector,
                                                @RegisterCompany override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                @NoUpdateContactAddress override val allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressFormProvider,
                                                val countryOptions: CountryOptions,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: manualAddress
                                               )(implicit val executionContext: ExecutionContext) extends ManualAddressController {

  override protected val form: Form[Address] = formProvider("error.country.invalid")

  private def addressViewModel(mode: Mode)(implicit request: DataRequest[AnyContent]): Retrieval[ManualAddressViewModel] = Retrieval(
    implicit request =>
      BusinessNameId.retrieve.right.map { companyName =>
        ManualAddressViewModel(
          routes.CompanyContactAddressController.onSubmit(mode),
          countryOptions.options,
          Message("enter.address.heading", Message("theCompany")),
          Message("enter.address.heading", companyName),
          psaName = psaName()
        )
      }
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      addressViewModel(mode).retrieve.right.map(vm =>
        get(vm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      addressViewModel(mode).retrieve.right.map(vm => {
        post(CompanyContactAddressId, vm, mode)
      }
        )
  }

}
