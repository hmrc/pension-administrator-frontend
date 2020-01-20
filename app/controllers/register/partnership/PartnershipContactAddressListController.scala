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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership._
import javax.inject.Inject
import models.{Mode, TolerantAddress}
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.ExecutionContext

class PartnershipContactAddressListController @Inject()(
                                                         val cacheConnector: UserAnswersCacheConnector,
                                                         @Partnership val navigator: Navigator,
                                                         val appConfig: FrontendAppConfig,
                                                         override val allowAccess: AllowAccessActionProvider,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: AddressListFormProvider,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: addressList
                                                       )(implicit val executionContext: ExecutionContext
                                                       ) extends AddressListController with Retrievals {


  def form(addresses: Seq[TolerantAddress], name: String)(implicit request: DataRequest[AnyContent]): Form[Int] =
    formProvider(addresses, Message("select.address.required.error").withArgs(name))

  def viewModel(mode: Mode) = Retrieval { implicit request =>
    PartnershipContactAddressPostCodeLookupId.retrieve.right map { addresses =>
      AddressListViewModel(
        routes.PartnershipContactAddressListController.onSubmit(mode),
        routes.PartnershipContactAddressController.onPageLoad(mode),
        addresses,
        Message("select.address.heading", Message("thePartnership").resolve),
        Message("select.address.heading", entityName),
        psaName = psaName()
      )
    }
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map { vm =>
        get(vm, mode, form(vm.addresses, entityName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map { vm =>
        post(vm, PartnershipContactAddressId, PartnershipContactAddressPostCodeLookupId, mode, form(vm.addresses, entityName))
      }
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))

}
