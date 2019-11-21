/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.company.directors._
import models.requests.DataRequest
import models.{Index, Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class DirectorPreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      override val cacheConnector: UserAnswersCacheConnector,
                                                      @CompanyDirector override val navigator: Navigator,
                                                      override val allowAccess: AllowAccessActionProvider,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: AddressListFormProvider) extends AddressListController with Retrievals {

  def form(addresses: Seq[TolerantAddress], name: String): Form[Int] =
    formProvider(addresses, Message("select.previous.address.error.required").withArgs(name))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map { vm =>
        get(vm, mode, form(vm.addresses, entityName(index)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(vm => post(vm, DirectorPreviousAddressListId(index), DirectorPreviousAddressId(index),
        mode, form(vm.addresses, entityName(index))))
  }

  private def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    DirectorPreviousAddressPostCodeLookupId(index).retrieve.right.map { addresses =>
      AddressListViewModel(
        postCall = routes.DirectorPreviousAddressListController.onSubmit(mode, index),
        manualInputCall = routes.DirectorPreviousAddressController.onPageLoad(mode, index),
        addresses = addresses,
        Message("previousAddressList.heading", Message("theDirector").resolve),
        Message("previousAddressList.heading", entityName(index)),
        Message("common.selectAddress.text"),
        Message("common.selectAddress.link"),
        psaName()
      )
    }.left.map(_ => Future.successful(Redirect(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index))))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

}
