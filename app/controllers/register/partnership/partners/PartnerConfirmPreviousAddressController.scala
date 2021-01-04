/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.partnership.partners._
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipPartner
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.ExecutionContext

class PartnerConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                        val dataCacheConnector: UserAnswersCacheConnector,
                                                        @PartnershipPartner val navigator: Navigator,
                                                        authenticate: AuthAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        val countryOptions: CountryOptions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: sameContactAddress
                                                       )(implicit val executionContext: ExecutionContext
)                                                       extends ConfirmPreviousAddressController with I18nSupport {
  private[controllers] val title: Message = "confirmPreviousAddress.title"
  private[controllers] val heading: Message = "confirmPreviousAddress.heading"

  private def viewmodel(mode: Mode, index: Index) =
    Retrieval(
      implicit request =>
        (PartnerNameId(index) and ExistingCurrentAddressId(index)).retrieve.right.map {
          case details ~ address =>
            SameContactAddressViewModel(
              controllers.register.partnership.partners.routes.PartnerConfirmPreviousAddressController.onSubmit(index),
              title = Message(title),
              heading = Message(heading, details.fullName),
              hint = None,
              address = address,
              psaName = details.fullName,
              mode = mode
            )
        }
    )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map { vm =>
        get(PartnerConfirmPreviousAddressId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map { vm =>
        post(PartnerConfirmPreviousAddressId(index), PartnerPreviousAddressId(index), vm, mode)
      }
  }
}
