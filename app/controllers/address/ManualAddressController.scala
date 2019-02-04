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

package controllers.address

import audit.{AddressEvent, AuditService}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.{Retrievals, Variations}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsNull, JsValue}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

trait ManualAddressController extends FrontendController with Retrievals with I18nSupport with Variations {

  protected def appConfig: FrontendAppConfig

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def auditService: AuditService

  protected val form: Form[Address]

  protected def get(
                     id: TypedIdentifier[Address],
                     selectedId: TypedIdentifier[TolerantAddress],
                     viewModel: ManualAddressViewModel
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id) match {
      case None => request.userAnswers.get(selectedId) match {
        case Some(value) => form.fill(value.toAddress)
        case None => form
      }
      case Some(value) => form.fill(value)
    }
    Future.successful(Ok(manualAddress(appConfig, preparedForm, viewModel)))
  }

  protected def post(
                      id: TypedIdentifier[Address],
                      selectedId: TypedIdentifier[TolerantAddress],
                      viewModel: ManualAddressViewModel,
                      mode: Mode,
                      context: String,
                      postCodeLookupIdForCleanup: TypedIdentifier[Seq[TolerantAddress]]
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithError: Form[_]) => Future.successful(BadRequest(manualAddress(appConfig, formWithError, viewModel))),
      address => {
        val existingAddress = request.userAnswers.get(id)
        val selectedAddress = request.userAnswers.get(selectedId)

        val auditEvent = AddressEvent.addressEntryEvent(request.externalId, address, existingAddress, selectedAddress, context)

        dataCacheConnector.remove(request.externalId, postCodeLookupIdForCleanup)
          .flatMap { _ =>
            dataCacheConnector.save(
              request.externalId,
              id,
              address
            ).flatMap { _ =>
                updateElement(mode, id)
                  .map {
                    cacheMap =>
                      auditEvent.foreach(auditService.sendEvent(_))
                      Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
                  }
              }
          }
      }
    )
  }

}
