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

package controllers.register.partnership

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.SameContactAddressController
import forms.address.SameContactAddressFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.BusinessNameId
import identifiers.register.partnership._
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, Partnership}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.ExecutionContext

@Singleton
class PartnershipSameContactAddressController @Inject()(
                                                         @Partnership val navigator: Navigator,
                                                         val appConfig: FrontendAppConfig,
                                                         val dataCacheConnector: UserAnswersCacheConnector,
                                                         authenticate: AuthAction,
                                                         @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: SameContactAddressFormProvider,
                                                         val countryOptions: CountryOptions,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: sameContactAddress
                                                         )(implicit val executionContext: ExecutionContext
                                                         ) extends SameContactAddressController {

  def form(name: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider(Message("same.contact.address.error").withArgs(name))

  private def viewmodel(mode: Mode, address: TolerantAddress, name: String)(implicit request: DataRequest[AnyContent]) =
    SameContactAddressViewModel(
      postCall = routes.PartnershipSameContactAddressController.onSubmit(mode),
      title = Message("partnership.sameContactAddress.title"),
      heading = Message("partnership.sameContactAddress.heading").withArgs(name),
      hint = None,
      address = address,
      psaName = name,
      mode = mode,
      displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      (PartnershipRegisteredAddressId and BusinessNameId).retrieve.right.map {
        case address ~ name =>
          get(PartnershipSameContactAddressId, viewmodel(mode, address, name), form(name))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      (PartnershipRegisteredAddressId and BusinessNameId).retrieve.right.map {
        case address ~ name =>
          post(
            PartnershipSameContactAddressId,
            PartnershipContactAddressListId,
            PartnershipContactAddressId,
            viewmodel(mode, address, name),
            mode,
            form(name)
          )
      }
  }

}
