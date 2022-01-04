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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.OrganisationNameController
import forms.BusinessNameFormProvider
import identifiers.register.BusinessNameId
import models.Mode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{Message, OrganisationNameViewModel}
import views.html.organisationName

import scala.concurrent.ExecutionContext

class CompanyRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                @RegisterCompany override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: BusinessNameFormProvider,
                                                val cacheConnector: UserAnswersCacheConnector,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: organisationName
                                               )(implicit val executionContext: ExecutionContext) extends OrganisationNameController {

  override val form: Form[String] = formProvider()

  private def companyNameViewModel(mode: Mode) =
    OrganisationNameViewModel(
      routes.CompanyRegisteredNameController.onSubmit(mode),
      Message("companyNameNonUk.title"),
      Message("companyNameNonUk.heading")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(BusinessNameId, companyNameViewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(BusinessNameId, companyNameViewModel(mode))
  }
}
