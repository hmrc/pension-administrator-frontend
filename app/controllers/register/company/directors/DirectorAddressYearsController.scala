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
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.company.directors.{DirectorAddressYearsId, DirectorNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class DirectorAddressYearsController @Inject()(
                                                @CompanyDirector override val navigator: Navigator,
                                                override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                override val cacheConnector: UserAnswersCacheConnector,
                                                override val allowAccess: AllowAccessActionProvider,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddressYearsFormProvider
                                              ) extends AddressYearsController with Retrievals {

  private def form(directorName: String): Form[AddressYears] = formProvider(directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        get(DirectorAddressYearsId(index), form(directorName), viewModel(mode, index, directorName), mode)
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val directorName = entityName(index)
      post(DirectorAddressYearsId(index), mode, form(directorName), viewModel(mode, index, directorName))
  }

  private def entityName(index: Int)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector").resolve)

  private def viewModel(mode: Mode, index: Index, directorName: String)(implicit request: DataRequest[AnyContent]): AddressYearsViewModel =
    AddressYearsViewModel(
      postCall = routes.DirectorAddressYearsController.onSubmit(mode, index),
      title = Message("addressYears.heading", Message("theDirector").resolve),
      heading = Message("addressYears.heading", directorName),
      legend = Message("addressYears.heading", directorName),
      psaName = psaName()
    )

}
