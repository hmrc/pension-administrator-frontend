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

package controllers.register.partnership.partners

import com.google.inject.Inject
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.partnership.partners._
import models.requests.DataRequest
import models.{Index, Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartner}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class PartnerPreviousAddressListController @Inject()(
                                                     override val cacheConnector: UserAnswersCacheConnector,
                                                     @PartnershipPartner override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddressListFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: addressList
                                                    )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def form(addresses: Seq[TolerantAddress], name: String)(implicit request: DataRequest[AnyContent]): Form[Int] =
    formProvider(addresses, Message("select.address.required.error", name))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.flatMap { pn =>
        viewModel(mode, index, pn.fullName).map { vm =>
          get(vm, mode, form(vm.addresses, pn.fullName))
        }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.flatMap { pn =>
        viewModel(mode, index, pn.fullName).map(vm =>
          post(vm, PartnerPreviousAddressId(index), PartnerPreviousAddressListId(index),
            PartnerPreviousAddressPostCodeLookupId(index), mode, form(vm.addresses, pn.fullName)))
      }
  }

  private def viewModel(mode: Mode, index: Index, name: String)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    PartnerPreviousAddressPostCodeLookupId(index).retrieve.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.PartnerPreviousAddressListController.onSubmit(mode, index),
          manualInputCall = routes.PartnerPreviousAddressController.onPageLoad(mode, index),
          addresses = addresses,
          Message("select.previous.address.heading", Message("thePartner")),
          Message("select.previous.address.heading", name),
          Message("select.address.hint.text"),
          Message("manual.entry.link"),
          psaName = psaName()
        )
    }.left.map(_ => Future.successful(Redirect(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))))
  }

}
