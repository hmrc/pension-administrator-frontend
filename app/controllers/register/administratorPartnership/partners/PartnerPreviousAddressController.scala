/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.partnership.partners.{PartnerNameId, PartnerPreviousAddressId, PartnerPreviousAddressListId}
import models.requests.DataRequest
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartnerV2}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class PartnerPreviousAddressController @Inject()(
                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                 @PartnershipPartnerV2 override val navigator: Navigator,
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
  private val isUkHintText = false
  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.map { pn =>
        get(PartnerPreviousAddressId(index), PartnerPreviousAddressListId(index), addressViewModel(mode, index, pn.fullName), mode, isUkHintText)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.map { pn =>
        val vm = addressViewModel(mode, index, pn.fullName)
        post(PartnerPreviousAddressId(index), vm, mode, isUkHintText)
      }
  }

  private def addressViewModel(mode: Mode, index: Index, name:String)(implicit request: DataRequest[AnyContent]) =
    ManualAddressViewModel(
      routes.PartnerPreviousAddressController.onSubmit(mode, index),
      countryOptions.options,
      Message("enter.previous.address.heading", Message("thePartner")),
      Message("enter.previous.address.heading", name),
      None,
      psaName = psaName(),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )

}
