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
import controllers.Retrievals
import forms.address.ConfirmPreviousAddressFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.{ExecutionContext, Future}

trait ConfirmPreviousAddressController extends FrontendBaseController with Retrievals {
  implicit val executionContext: ExecutionContext

  protected def appConfig: FrontendAppConfig

  protected def controllerComponents: MessagesControllerComponents

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected def formProvider: ConfirmPreviousAddressFormProvider = new ConfirmPreviousAddressFormProvider()

  protected def countryOptions: CountryOptions

  protected  def view: sameContactAddress

  protected def form(name: String)(implicit mesages: Messages): Form[Boolean] =
    formProvider(Message("confirmPreviousAddress.error", name))

  protected def get(id: TypedIdentifier[Boolean], viewModel: SameContactAddressViewModel)
                   (implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {

    val preparedForm = request.userAnswers.get(id) match {
      case None => form(viewModel.psaName)(implicitly)
      case Some(value) => form(viewModel.psaName)(implicitly).fill(value)
    }
    Future.successful(Ok(view(preparedForm, viewModel, countryOptions)))
  }

  protected def post(id: TypedIdentifier[Boolean], contactId: TypedIdentifier[Address], viewModel: SameContactAddressViewModel, mode: Mode)
                    (implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {

    form(viewModel.psaName)(implicitly).bindFromRequest().fold(
      formWithError => Future.successful(BadRequest(view(formWithError, viewModel, countryOptions))),
      { case true => dataCacheConnector.save(request.externalId, id, true).flatMap { _ =>
        dataCacheConnector.save(request.externalId, contactId, viewModel.address.toAddress.get).map {
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
