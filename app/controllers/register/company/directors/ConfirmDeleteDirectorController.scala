/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.register.company.routes.AddCompanyDirectorsController
import controllers.{ConfirmDeleteController, Retrievals}
import forms.ConfirmDeleteFormProvider
import identifiers.register.company.directors.DirectorNameId
import models.FeatureToggleName.PsaRegistration

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

import scala.concurrent.ExecutionContext

class ConfirmDeleteDirectorController @Inject()(val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                val allowAccess: AllowAccessActionProvider,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val cacheConnector: UserAnswersCacheConnector,
                                                formProvider: ConfirmDeleteFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: confirmDelete,
                                                featureToggleConnector: FeatureToggleConnector
                                               )(implicit val executionContext: ExecutionContext) extends ConfirmDeleteController with Retrievals {

  def form(directorName: String)(implicit messages: Messages): Form[Boolean] = formProvider(directorName)

  private def vm(index: Index, name: String, mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) = ConfirmDeleteViewModel(
    routes.ConfirmDeleteDirectorController.onSubmit(mode, index),
    controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode),
    Message("confirmDeleteDirector.title"),
    "confirmDeleteDirector.heading",
    name,
    None,
    psaName(),
    returnLink
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
        val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
        DirectorNameId(index).retrieve.map { details =>
          get(vm(index, details.fullName, mode, returnLink), details.isDeleted, routes.AlreadyDeletedController.onPageLoad(index), mode)
        }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
        val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
        DirectorNameId(index).retrieve.map { details =>
          post(vm(index, details.fullName, mode, returnLink), DirectorNameId(index), AddCompanyDirectorsController.onPageLoad(mode), mode)
        }
      }
  }

}
