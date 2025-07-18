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

package controllers.register.administratorPartnership.contactDetails

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.{PartnershipPreviousAddressId, PartnershipPreviousAddressListId, PartnershipPreviousAddressPostCodeLookupId}
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipV2}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class PartnershipPreviousAddressListController @Inject()(
                                                          @PartnershipV2 val navigator: Navigator,
                                                          override val cacheConnector: UserAnswersCacheConnector,
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

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).map { vm =>
        get(vm, mode, form(vm.addresses, entityName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).map(vm => post(vm, PartnershipPreviousAddressId, PartnershipPreviousAddressListId, PartnershipPreviousAddressPostCodeLookupId, mode,
        form(vm.addresses, entityName)))
  }

  private def viewmodel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    PartnershipPreviousAddressPostCodeLookupId.retrieve.map { addresses =>
      AddressListViewModel(
        postCall = routes.PartnershipPreviousAddressListController.onSubmit(mode),
        manualInputCall = routes.PartnershipPreviousAddressController.onPageLoad(mode),
        addresses = addresses,
        Message("select.previous.address.heading", Message("thePartnership")),
        Message("select.previous.address.heading", entityName),
        Message("select.address.hint.text"),
        Message("manual.entry.link"),
        partnershipName = Some(entityName),
        returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
      )
    }.left.map(_ => Future.successful(Redirect(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode))))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))
}
