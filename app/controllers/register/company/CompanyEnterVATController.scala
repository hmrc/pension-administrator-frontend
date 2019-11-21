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

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.VATNumberController
import forms.register.EnterVATFormProvider
import identifiers.register.{BusinessNameId, EnterVATId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterVAT

import scala.concurrent.ExecutionContext

class CompanyEnterVATController @Inject()(val appConfig: FrontendAppConfig,
                                          val cacheConnector: UserAnswersCacheConnector,
                                          @RegisterCompany val navigator: Navigator,
                                          authenticate: AuthAction,
                                          allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: EnterVATFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: enterVAT
                                         )(implicit val executionContext: ExecutionContext) extends VATNumberController {

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(companyName)

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyEnterVATController.onSubmit(mode),
      title = Message("enterVAT.title", Message("theCompany")),
      heading = Message("enterVAT.heading", entityName),
      mode = mode,
      entityName = entityName
    )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(EnterVATId, form(entityName), viewModel(mode, entityName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(EnterVATId, mode, form(entityName), viewModel(mode, entityName))
  }
}
