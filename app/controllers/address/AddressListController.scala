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

package controllers.address

import config.FrontendAppConfig
import connectors.DataCacheConnector
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode, TolerantAddress}
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

trait AddressListController extends FrontendController with I18nSupport {

  protected def appConfig: FrontendAppConfig
  protected def cacheConnector: DataCacheConnector
  protected def navigator: Navigator
  protected def formProvider: AddressListFormProvider = new AddressListFormProvider()

  protected def get(viewModel: AddressListViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val form = formProvider(viewModel.addresses)
    Future.successful(Ok(addressList(appConfig, form, viewModel)))
  }

  protected def post(viewModel: AddressListViewModel, navigatorId: TypedIdentifier[TolerantAddress], dataId: TypedIdentifier[Address], mode: Mode)
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {

    formProvider(viewModel.addresses).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(addressList(appConfig, formWithErrors, viewModel))),
      addressIndex => {
        val address = viewModel.addresses(addressIndex).copy(country = Some("GB"))

        cacheConnector.remove(request.externalId, dataId).flatMap {
          _ =>
            cacheConnector.save(request.externalId, navigatorId, address).map {
              json =>
                Redirect(navigator.nextPage(navigatorId, mode)(UserAnswers(json)))
            }
        }
      }
    )

  }

}
