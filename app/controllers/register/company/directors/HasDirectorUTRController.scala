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
import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.company.directors.routes.HasDirectorUTRController
import forms.HasReferenceNumberFormProvider
import identifiers.register.company.directors.{DirectorNameId, HasDirectorUTRId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class HasDirectorUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val dataCacheConnector: UserAnswersCacheConnector,
                                         @CompanyDirector override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasReferenceNumberFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: hasReferenceNumber
                                         )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController with I18nSupport {

  private def viewModel(mode: Mode, entityName: String, index: Index): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = HasDirectorUTRController.onSubmit(mode, index),
      title = Message("hasUTR.heading", Message("theDirector")),
      heading = Message("hasUTR.heading", entityName),
      mode = mode,
      hint = Some(Message("utr.p1")),
      entityName = entityName
    )

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def form(directorName: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("hasUTR.error.required", directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        get(HasDirectorUTRId(index), form(directorName), viewModel(mode, directorName, index))

    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        post(HasDirectorUTRId(index), mode, form(directorName), viewModel(mode, directorName, index))
    }
}
