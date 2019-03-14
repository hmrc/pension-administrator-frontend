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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.company.directors
import identifiers.register.company.directors.{DirectorConfirmPreviousAddressId, DirectorDetailsId, DirectorPreviousAddressId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel

class DirectorConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                         val messagesApi: MessagesApi,
                                                         val dataCacheConnector: UserAnswersCacheConnector,
                                                         @CompanyDirector val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         allowAccess: AllowAccessActionProvider,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         val countryOptions: CountryOptions
                                                       ) extends ConfirmPreviousAddressController with I18nSupport {
  private[controllers] val title: Message = "confirmPreviousAddress.title"
  private[controllers] val heading: Message = "confirmPreviousAddress.heading"

  private def viewmodel(mode: Mode, index: Index) =
    Retrieval(
      implicit request =>
        (DirectorDetailsId(index) and directors.ExistingCurrentAddressId(index)).retrieve.right.map {
          case details ~ address =>
            SameContactAddressViewModel(
              controllers.register.company.directors.routes.DirectorConfirmPreviousAddressController.onSubmit(index),
              title = Message(title),
              heading = Message(heading, details.fullName),
              hint = None,
              secondaryHeader = None,
              address = address,
              psaName = details.fullName,
              mode = mode
            )
        }
    )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map { vm =>
        get(DirectorConfirmPreviousAddressId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).retrieve.right.map { vm =>
        post(DirectorConfirmPreviousAddressId(index), DirectorPreviousAddressId(index), vm, mode)
      }
  }
}