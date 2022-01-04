/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.Variations
import controllers.actions.AllowAccessActionProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{UserAnswers, Navigator}
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

trait AddressListController extends FrontendBaseController with I18nSupport with Variations {

  implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected val allowAccess: AllowAccessActionProvider

  protected def view: addressList

  protected def get(viewModel: AddressListViewModel, mode: Mode, form: Form[Int])
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {
    Future.successful(Ok(view(form, viewModel, mode)))
  }


  protected def post(viewModel: AddressListViewModel, addressId: TypedIdentifier[Address],
                     selectAddressId: TypedIdentifier[TolerantAddress],
                     postCodeLookupIdForCleanUp: TypedIdentifier[Seq[TolerantAddress]],
                     mode: Mode, form: Form[Int])
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {

    def performUpsert(userAnswers: UserAnswers)(block: JsValue => Future[Result]):Future[Result] =
      cacheConnector.upsert(request.externalId, userAnswers.json).flatMap(block)

    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, viewModel, mode))),
      addressIndex => {
        val address = viewModel.addresses(addressIndex).copy(countryOpt = Some("GB"))
        address.toAddress match {
          case Some(addr) =>
            val userAnswers = request.userAnswers.set(addressId)(addr).flatMap(
              _.remove(postCodeLookupIdForCleanUp)).asOpt.getOrElse(request.userAnswers)
            performUpsert(userAnswers) ( cacheMap =>
              saveChangeFlag(mode, addressId)
                .flatMap(_ => Future.successful(Redirect(navigator.nextPage(addressId, mode, UserAnswers(cacheMap)))))
            )
          case None =>
            val userAnswers = request.userAnswers.remove(addressId)
              .flatMap(_.set(selectAddressId)(address)).asOpt.getOrElse(request.userAnswers)
            performUpsert(userAnswers)( _ => Future.successful(Redirect(viewModel.manualInputCall)))
        }
      }
    )
  }
}
