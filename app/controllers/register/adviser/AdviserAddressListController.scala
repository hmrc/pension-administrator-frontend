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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.adviser._
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.Adviser
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class AdviserAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             override val cacheConnector: UserAnswersCacheConnector,
                                             @Adviser override val navigator: Navigator,
                                             override val allowAccess: AllowAccessActionProvider,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map{vm =>
        get(vm, mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map(vm => post(vm, AdviserAddressListId, AdviserAddressId, mode))
  }

  def viewmodel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    AdviserAddressPostCodeLookupId.retrieve.right.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.AdviserAddressListController.onSubmit(mode),
          manualInputCall = routes.AdviserAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("adviserAddressList.heading", Message("theAdviser")),
          Message("adviserAddressList.heading", entityName),
          Message("common.selectAddress.text"),
          Message("common.selectAddress.link"),
          psaName = psaName()
        )
    }.left.map(_ => Future.successful(Redirect(routes.AdviserAddressPostCodeLookupController.onPageLoad(mode))))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))
}
