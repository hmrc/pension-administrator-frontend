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

package controllers.register.adviser

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser._
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.{Adviser, NoRLSCheck}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdviserAddressListController @Inject()(
                                             override val cacheConnector: UserAnswersCacheConnector,
                                             @Adviser override val navigator: Navigator,
                                             @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: AddressListFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: addressList
                                            )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def form(addresses: Seq[TolerantAddress], name: String)(implicit request: DataRequest[AnyContent]): Form[Int] =
    formProvider(addresses, Message("select.address.required.error").withArgs(name))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl())).map { vm =>
        get(vm, mode, form(vm.addresses, entityName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl()))
        .map(vm => post(vm, AdviserAddressId, AdviserAddressListId, AdviserAddressPostCodeLookupId, mode, form(vm.addresses, entityName)))
  }

  def viewModel(mode: Mode, displayReturnLink: Boolean, returnLink: Option[String])
               (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    AdviserAddressPostCodeLookupId.retrieve.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.AdviserAddressListController.onSubmit(mode),
          manualInputCall = routes.AdviserAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("select.address.heading", Message("theAdviser")),
          Message("select.address.heading", entityName),
          Message("select.address.hint.text"),
          Message("manual.entry.link"),
          psaName = if (displayReturnLink) psaName() else None,
          returnLink = returnLink
        )
    }.left.map(_ => Future.successful(Redirect(routes.AdviserAddressPostCodeLookupController.onPageLoad(mode))))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))
}
