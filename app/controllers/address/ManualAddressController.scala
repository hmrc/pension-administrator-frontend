/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.actions.AllowAccessActionProvider
import controllers.{Retrievals, Variations}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.Future

trait ManualAddressController extends FrontendBaseController with Retrievals with I18nSupport with Variations {

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected val form: Form[Address]

  protected val allowAccess: AllowAccessActionProvider

  protected def view: manualAddress

  protected def get(
                     id: TypedIdentifier[Address],
                     selectedId: TypedIdentifier[TolerantAddress],
                     viewModel: ManualAddressViewModel,
                     mode: Mode
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.get(id) match {
      case None => request.userAnswers.get(selectedId) match {
        case Some(value) => form.fill(value.toPrepopAddress)
        case None => form
      }
      case Some(value) => form.fill(value)
    }
    Future.successful(Ok(view(preparedForm, viewModel, mode)))
  }

  protected def post(
                      id: TypedIdentifier[Address],
                      viewModel: ManualAddressViewModel,
                      mode: Mode
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithError: Form[_]) => Future.successful(BadRequest(view(formWithError, viewModel, mode))),
      address => {
        cacheConnector.save(request.externalId, id, address).flatMap { userAnswersJson =>
          saveChangeFlag(mode, id)
            .flatMap {
              _ =>
                Future.successful(Redirect(navigator.nextPage(id, mode, UserAnswers(userAnswersJson))))
            }
        }
      }
    )
  }
}
