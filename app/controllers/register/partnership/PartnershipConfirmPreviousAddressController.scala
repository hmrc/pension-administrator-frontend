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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.UpdateContactAddressId
import identifiers.register.BusinessNameId
import identifiers.register.partnership.{ExistingCurrentAddressId, PartnershipConfirmPreviousAddressId, PartnershipPreviousAddressId}
import javax.inject.Inject
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, Partnership}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.ExecutionContext

class PartnershipConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                            val dataCacheConnector: UserAnswersCacheConnector,
                                                            @Partnership val navigator: Navigator,
                                                            authenticate: AuthAction,
                                                            @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            val countryOptions: CountryOptions,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            val view: sameContactAddress
                                                            )(implicit val executionContext: ExecutionContext
                                                            ) extends ConfirmPreviousAddressController with I18nSupport {

  private[controllers] val postCall = routes.PartnershipConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "confirmPreviousAddress.title"
  private[controllers] val heading: Message = "confirmPreviousAddress.heading"

  private def viewmodel(mode: Mode) =
    Retrieval(
      implicit request =>
        (BusinessNameId and ExistingCurrentAddressId).retrieve.map {
          case name ~ address =>
            SameContactAddressViewModel(
              postCall(),
              title = Message(title),
              heading = Message(heading, name),
              hint = None,
              address = address,
              psaName = name,
              mode = mode,
              displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
            )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.map { vm =>
        get(PartnershipConfirmPreviousAddressId, vm)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.map { vm =>
        post(PartnershipConfirmPreviousAddressId, PartnershipPreviousAddressId, vm, mode)
      }
  }

}
