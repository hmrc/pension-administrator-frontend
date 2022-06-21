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

package controllers.register.administratorPartnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.OrganisationNameController
import forms.BusinessNameFormProvider
import identifiers.register.BusinessNameId
import models.Mode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipV2
import viewmodels.{Message, OrganisationNameViewModel}
import views.html.organisationName

import scala.concurrent.ExecutionContext

class PartnershipRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                    @PartnershipV2 val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: BusinessNameFormProvider,
                                                    val cacheConnector: UserAnswersCacheConnector,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: organisationName
                                                   )(implicit val executionContext: ExecutionContext) extends OrganisationNameController {

  override val form = formProvider(
    requiredKey = "partnershipName.error.required",
    invalidKey = "partnershipName.error.invalid",
    lengthKey = "partnershipName.error.length")

  private def partnershipNameViewModel =
    OrganisationNameViewModel(
      routes.PartnershipRegisteredNameController.onSubmit(),
      Message("partnershipName.title"),
      Message("partnershipName.heading")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(BusinessNameId, partnershipNameViewModel)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(BusinessNameId, partnershipNameViewModel)
  }
}
