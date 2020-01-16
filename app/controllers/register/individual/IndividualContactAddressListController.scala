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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import forms.address.AddressListFormProvider
import identifiers.register.individual.{IndividualContactAddressId, IndividualContactAddressListId, IndividualContactAddressPostCodeLookupId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.Individual
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class IndividualContactAddressListController @Inject()(@Individual override val navigator: Navigator,
                                                       override val appConfig: FrontendAppConfig,
                                                       override val cacheConnector: UserAnswersCacheConnector,
                                                       authenticate: AuthAction,
                                                       override val allowAccess: AllowAccessActionProvider,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: AddressListFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       val view: addressList
                                                      )(implicit val executionContext: ExecutionContext
                                                      ) extends AddressListController with Retrievals with I18nSupport {

  def form(addresses: Seq[TolerantAddress])(implicit request: DataRequest[AnyContent]): Form[Int] = formProvider(addresses, Message("select.address.required.error"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map{vm =>
        get(vm, mode, form(vm.addresses))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map(vm => post(vm, IndividualContactAddressId, IndividualContactAddressPostCodeLookupId, mode, form(vm.addresses)))
  }


  private def viewmodel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    IndividualContactAddressPostCodeLookupId.retrieve.right.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.IndividualContactAddressListController.onSubmit(mode),
          manualInputCall = routes.IndividualContactAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("individual.select.address.heading"),
          Message("individual.select.address.heading"),
          Message("select.address.hint.text"),
          Message("manual.entry.link"),
          psaName = psaName(),
          selectAddressPostLink = Some(Message("individual.selectAddressPostLink.text"))
        )
    }.left.map(_ => Future.successful(Redirect(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode))))
  }


}
