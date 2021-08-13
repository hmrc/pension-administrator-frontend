/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.{PartnershipContactAddressId, PartnershipContactAddressListId}
import models.requests.DataRequest
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.NoRLSCheck
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class PartnershipContactAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                    val cacheConnector: UserAnswersCacheConnector,
                                                    @Partnership val navigator: Navigator,
                                                    @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: AddressFormProvider,
                                                    val countryOptions: CountryOptions,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: manualAddress
                                                   )(implicit val executionContext: ExecutionContext) extends ManualAddressController with I18nSupport {

  protected val form: Form[Address] = formProvider("error.country.invalid")

  def viewmodel(mode: Mode, partnershipName: String)(implicit request: DataRequest[AnyContent]) =
    ManualAddressViewModel(
      postCall = routes.PartnershipContactAddressController.onSubmit(mode),
      countryOptions = countryOptions.options,
      title = Message("enter.address.heading").withArgs(Message("thePartnership")),
      heading = Message("enter.address.heading").withArgs(partnershipName),
      psaName = psaName()
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map {
        name =>
          get(
            PartnershipContactAddressId,
            PartnershipContactAddressListId,
            viewmodel(mode, name),
            mode
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map {
        name =>
          post(
            PartnershipContactAddressId,
            viewmodel(mode, name),
            mode
          )
      }
  }
}
