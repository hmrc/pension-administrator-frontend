/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partners

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.UpdateContactAddressId
import identifiers.register.partnership.partners._
import models.{Index, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartnerV2}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerConfirmPreviousAddressController @Inject()(
                                                         val dataCacheConnector: UserAnswersCacheConnector,
                                                         @PartnershipPartnerV2 val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         val countryOptions: CountryOptions,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: sameContactAddress
                                                       )(implicit val executionContext: ExecutionContext)
  extends ConfirmPreviousAddressController with I18nSupport {
  private[controllers] val title: Message = "confirmPreviousAddress.title"
  private[controllers] val heading: Message = "confirmPreviousAddress.heading"

  private def viewModel(mode: Mode, index: Index): Retrieval[SameContactAddressViewModel] =
    Retrieval(
      implicit request =>
        (PartnerNameId(index) and ExistingCurrentAddressId(index)).retrieve.map {
          case details ~ address =>
            SameContactAddressViewModel(
              controllers.register.administratorPartnership.partners.routes.PartnerConfirmPreviousAddressController.onSubmit(index),
              title = Message(title),
              heading = Message(heading, details.fullName),
              hint = None,
              address = address,
              psaName = details.fullName,
              mode = mode,
              displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
            )
        }
    )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).retrieve.map { vm =>
        get(PartnerConfirmPreviousAddressId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).retrieve.map { vm =>
        post(PartnerConfirmPreviousAddressId(index), PartnerPreviousAddressId(index), vm, mode)
      }
  }
}
