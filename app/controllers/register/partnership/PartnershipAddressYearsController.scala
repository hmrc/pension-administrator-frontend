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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.partnership.{PartnershipAddressYearsId, PartnershipDetailsId}
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class PartnershipAddressYearsController @Inject()(
                                                   val appConfig: FrontendAppConfig,
                                                   val cacheConnector: UserAnswersCacheConnector,
                                                   @Partnership val navigator: Navigator,
                                                   val messagesApi: MessagesApi,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressYearsFormProvider
                                                 ) extends AddressYearsController with Retrievals {


  private def viewModel(mode: Mode) =
    Retrieval {
      implicit request =>
        PartnershipDetailsId.retrieve.right.map { details =>
          AddressYearsViewModel(
            routes.PartnershipAddressYearsController.onSubmit(mode),
            Message("partnership.addressYears.title"),
            Message("partnership.addressYears.heading").withArgs(details.companyName),
            Message("partnership.addressYears.heading").withArgs(details.companyName),
            None
          )
        }
    }

  val form = formProvider("error.addressYears.required")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map {
        get(PartnershipAddressYearsId, form, _)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map {
        post(PartnershipAddressYearsId, mode, form, _)
      }
  }
}
