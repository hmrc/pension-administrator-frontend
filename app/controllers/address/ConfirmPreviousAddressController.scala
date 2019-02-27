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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.Future

trait ConfirmPreviousAddressController extends FrontendController with Retrievals with I18nSupport {
  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected val form: Form[Boolean]

  protected def countryOptions: CountryOptions

  protected def get(
                     id: TypedIdentifier[Boolean],
                     viewModel: SameContactAddressViewModel
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form
      case Some(value) => form.fill(value)
    }
    Future.successful(Ok(sameContactAddress(appConfig, preparedForm, viewModel, countryOptions)))
  }

  protected def post(
                      id: TypedIdentifier[Boolean],
                      contactId: TypedIdentifier[Address],
                      viewModel: SameContactAddressViewModel,
                      mode: Mode
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      formWithError => Future.successful(BadRequest(sameContactAddress(appConfig, formWithError, viewModel, countryOptions))),
      { case true => dataCacheConnector.save(request.externalId, id, true).flatMap { _ =>
        dataCacheConnector.save(request.externalId, contactId, viewModel.address.toAddress).map {
          cacheMap =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap)))
        }
      }
      case _ => dataCacheConnector.save(request.externalId, id, false).flatMap { cacheMap =>
        Future.successful(Redirect(navigator.nextPage(id, mode, UserAnswers(cacheMap))))
      }
      }
    )
  }
}
