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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.register.NINOController
import forms.register.NINOFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.directors.{DirectorEnterNINOId, DirectorNameId}
import models.FeatureToggleName.PsaRegistration

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterNINO

import scala.concurrent.ExecutionContext

class DirectorEnterNINOController @Inject()(@CompanyDirector val navigator: Navigator,
                                            val appConfig: FrontendAppConfig,
                                            val cacheConnector: UserAnswersCacheConnector,
                                            authenticate: AuthAction,
                                            val allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: NINOFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: enterNINO,
                                            featureToggleConnector: FeatureToggleConnector
                                           )(implicit val executionContext: ExecutionContext) extends NINOController {

  private def form(directorName: String)
                  (implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
          val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
          get(DirectorEnterNINOId(index), form(directorName), viewModel(mode, index, directorName, returnLink))
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val directorName = entityName(index)
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        post(DirectorEnterNINOId(index), mode, form(directorName), viewModel(mode, index, directorName, returnLink))
      }
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorEnterNINOController.onSubmit(mode, index),
      title = Message("enterNINO.heading", Message("theDirector")),
      heading = Message("enterNINO.heading", directorName),
      hint = Some(Message("enterNINO.hint")),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )
}
