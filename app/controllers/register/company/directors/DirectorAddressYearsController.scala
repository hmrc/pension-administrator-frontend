/*
 * Copyright 2018 HM Revenue & Customs
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
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.company.directors.{DirectorAddressYearsId, DirectorDetailsId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.Future

class DirectorAddressYearsController @Inject()(
                                                @CompanyDirector override val navigator: Navigator,
                                                override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                override val cacheConnector: UserAnswersCacheConnector,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressYearsFormProvider
                                              ) extends AddressYearsController with Retrievals {

  private val form: Form[AddressYears] = formProvider(Message("error.addressYears.required"))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(mode, index).right.map {
          viewModel =>
            get(DirectorAddressYearsId(index), form, viewModel)
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).right.map {
        viewModel =>
          post(DirectorAddressYearsId(index), mode, form, viewModel)
      }
  }

  private def viewmodel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressYearsViewModel] = {
    DirectorDetailsId(index).retrieve.right.map {
      director =>
        AddressYearsViewModel(
          postCall = routes.DirectorAddressYearsController.onSubmit(mode, index),
          title = Message("directorAddressYears.title"),
          heading = Message("directorAddressYears.heading"),
          legend = Message("directorAddressYears.heading"),
          Some(Message(director.fullName))
        )
    }
  }

}
