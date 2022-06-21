/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partners

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.partnership.partners.{PartnerAddressId, PartnerAddressListId, PartnerNameId}
import models.requests.DataRequest
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartner}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class PartnerAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val cacheConnector: UserAnswersCacheConnector,
                                         @PartnershipPartner override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddressFormProvider,
                                         countryOptions: CountryOptions,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: manualAddress
                                        )(implicit val executionContext: ExecutionContext) extends ManualAddressController with Retrievals {

  override protected val form: Form[Address] = formProvider()

  private def addressViewModel(mode: Mode, index: Index, name: String)(implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    routes.PartnerAddressController.onSubmit(mode, index),
    countryOptions.options,
    Message("enter.address.heading", Message("thePartner")),
    Message("enter.address.heading", name),
    psaName = psaName()
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.map { pn =>
        get(PartnerAddressId(index), PartnerAddressListId(index), addressViewModel(mode, index, pn.fullName), mode)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.map { pn =>
        val vm = addressViewModel(mode, index, pn.fullName)
        post(PartnerAddressId(index), vm, mode)
      }
  }

}
