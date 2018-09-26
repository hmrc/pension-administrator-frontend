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

package controllers.register.company.directors

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.company.directors.{CompanyDirectorAddressListId, CompanyDirectorAddressPostCodeLookupId, DirectorAddressId, DirectorDetailsId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class CompanyDirectorAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val cacheConnector: UserAnswersCacheConnector,
                                                     @CompanyDirector override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(vm => post(vm, CompanyDirectorAddressListId(index), DirectorAddressId(index), mode))
  }

  private def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    CompanyDirectorAddressPostCodeLookupId(index).retrieve.right.flatMap {
      addresses =>
        DirectorDetailsId(index).retrieve.right.map {
          director =>
            AddressListViewModel(
              postCall = routes.CompanyDirectorAddressListController.onSubmit(mode, index),
              manualInputCall = routes.DirectorAddressController.onPageLoad(mode, index),
              addresses = addresses,
              Message("common.selectAddress.title"),
              Message("common.selectAddress.heading"),
              Some(Message(director.fullName)),
              Message("common.selectAddress.text"),
              Message("common.selectAddress.link")
            )
        }
    }.left.map(_ => Future.successful(Redirect(routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index))))
  }

}
