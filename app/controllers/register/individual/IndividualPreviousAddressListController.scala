/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.individual._
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.Individual
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future


class IndividualPreviousAddressListController @Inject()(
                                                         @Individual override val navigator: Navigator,
                                                         override val appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         override val cacheConnector: UserAnswersCacheConnector,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map(get)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map(vm => post(vm, IndividualPreviousAddressListId, IndividualPreviousAddressId, mode))
  }

  private def viewmodel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    IndividualPreviousAddressPostCodeLookupId.retrieve.right.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.IndividualPreviousAddressListController.onSubmit(mode),
          manualInputCall = routes.IndividualPreviousAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("common.previousAddressList.title"),
          Message("common.previousAddressList.heading"),
          None,
          Message("common.selectAddress.text"),
          Message("common.selectAddress.link")
        )
    }.left.map(_ => Future.successful(Redirect(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(mode))))
  }
}
