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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.EnterPAYEController
import forms.EnterPAYEFormProvider
import identifiers.register.{BusinessNameId, EnterPAYEId}
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class CompanyEnterPAYEController @Inject()(val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           @RegisterCompany val navigator: Navigator,
                                           authenticate: AuthAction,
                                           allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EnterPAYEFormProvider
                                          )(implicit val ec: ExecutionContext) extends EnterPAYEController {

  protected def form(companyName: String): Form[String] = formProvider(companyName)

  private def viewModel(mode: Mode, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.company.routes.CompanyEnterPAYEController.onSubmit(mode),
      title = Message("enterPAYE.heading", Message("theCompany").resolve),
      heading = Message("enterPAYE.heading", companyName),
      mode = mode,
      hint = Some(Message("enterPAYE.hint")),
      entityName = companyName
    )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany").resolve)

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(EnterPAYEId, form(entityName), viewModel(mode, entityName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(EnterPAYEId, mode, form(entityName), viewModel(mode, entityName))
  }
}
