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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.CompanyNameController
import forms.CompanyNameFormProvider
import identifiers.register.company.CompanyNameId
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CompanyNameViewModel, Message}

class CompanyRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @RegisterCompany override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: CompanyNameFormProvider,
                                                val cacheConnector: UserAnswersCacheConnector) extends CompanyNameController {

  private def companyNameViewModel(mode: Mode) =
    CompanyNameViewModel(
      routes.CompanyRegisteredNameController.onSubmit(mode),
      Message("companyName.title"),
      Message("companyName.heading")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(CompanyNameId, companyNameViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyNameId, mode, companyNameViewModel(mode))
  }

}