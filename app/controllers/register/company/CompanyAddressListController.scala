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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.BusinessNameId
import identifiers.register.company.{CompanyAddressListId, CompanyPreviousAddressId, CompanyPreviousAddressPostCodeLookupId}
import models.Mode
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val cacheConnector: UserAnswersCacheConnector,
                                             @RegisterCompany override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             override val allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: addressList
                                            )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map{vm =>
        get(vm, mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).right.map(vm => post(vm, CompanyAddressListId, CompanyPreviousAddressId, mode))
  }

  def viewmodel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    (BusinessNameId and CompanyPreviousAddressPostCodeLookupId).retrieve.right.map {
      case name ~ addresses =>
        AddressListViewModel(
          postCall = routes.CompanyAddressListController.onSubmit(mode),
          manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(mode),
          addresses = addresses,
          Message("previousAddressList.heading", Message("theCompany").resolve),
          Message("previousAddressList.heading", name),
          Message("common.selectAddress.text"),
          Message("common.selectAddress.link"),
          psaName = psaName()
        )
    }.left.map(_ => Future.successful(Redirect(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode))))
  }

}
