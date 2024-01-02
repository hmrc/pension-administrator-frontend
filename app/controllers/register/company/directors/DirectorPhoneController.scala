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
import controllers.register.PhoneController
import forms.PhoneFormProvider
import identifiers.register.company.directors.{DirectorNameId, DirectorPhoneId}
import models.FeatureToggleName.PsaRegistration
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DirectorPhoneController @Inject()(@CompanyDirector val navigator: Navigator,
                                        val appConfig: FrontendAppConfig,
                                        val cacheConnector: UserAnswersCacheConnector,
                                        authenticate: AuthAction,
                                        val allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PhoneFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: phone,
                                        featureToggleConnector: FeatureToggleConnector
                                      )(implicit val executionContext: ExecutionContext) extends PhoneController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
          val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
          get(DirectorPhoneId(index), form, viewModel(mode, index, entityName(index), returnLink))
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
        val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
        post(DirectorPhoneId(index), mode, form, viewModel(mode, index, entityName(index), returnLink))
      }
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorPhoneController.onSubmit(mode, index),
      title = Message("phone.title", Message("theDirector")),
      heading = Message("phone.title", directorName),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )
}
