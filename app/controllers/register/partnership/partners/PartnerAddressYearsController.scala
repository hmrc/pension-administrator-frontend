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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.partnership.partners.{PartnerAddressYearsId, PartnerDetailsId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.Future

class PartnerAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               @PartnershipPartner val navigator: Navigator,
                                               val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressYearsFormProvider
                                             ) extends AddressYearsController with Retrievals {


  private val form: Form[AddressYears] = formProvider(Message("partnerAddressYears.error.required"))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(mode, index).right.map {
          viewModel =>
            get(PartnerAddressYearsId(index), form, viewModel)
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).right.map {
        viewModel =>
          post(PartnerAddressYearsId(index), mode, form, viewModel)
      }
  }

  private def viewmodel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressYearsViewModel] = {
    PartnerDetailsId(index).retrieve.right.map {
      partner =>
        AddressYearsViewModel(
          postCall = routes.PartnerAddressYearsController.onSubmit(mode, index),
          title = Message("partnerAddressYears.title"),
          heading = Message("partnerAddressYears.heading"),
          legend = Message("partnerAddressYears.heading"),
          Some(Message(partner.fullName))
        )
    }
  }
}
