/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.EnterUTRController
import controllers.actions._
import controllers.register.company.directors.routes.DirectorEnterUTRController
import forms.EnterUTRFormProvider
import identifiers.register.company.directors.{DirectorEnterUTRId, DirectorNameId}
import models.FeatureToggleName.PsaRegistration
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterUTR

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DirectorEnterUTRController @Inject()(@CompanyDirector val navigator: Navigator,
                                           val appConfig: FrontendAppConfig,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           authenticate: AuthAction,
                                           val allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EnterUTRFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: enterUTR,
                                           featureToggleConnector: FeatureToggleConnector
                                          )(implicit val executionContext: ExecutionContext) extends EnterUTRController {
  private def form(directorName: String)
                  (implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
          val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
          get(DirectorEnterUTRId(index), form(directorName), viewModel(mode, index, directorName, returnLink))
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val directorName = entityName(index)
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        post(DirectorEnterUTRId(index), mode, form(directorName), viewModel(mode, index, directorName, returnLink))
      }
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = DirectorEnterUTRController.onSubmit(mode, index),
      title = Message("enterUTR.heading", Message("theDirector")),
      heading = Message("enterUTR.heading", directorName),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )
}
