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

package controllers.register.company

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.VATNumberController
import forms.register.EnterVATFormProvider
import identifiers.register.EnterVATId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterVAT

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
                                         )(implicit val executionContext: ExecutionContext) extends VATNumberController with Retrievals {

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(companyName)

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyEnterVATController.onSubmit(mode),
      title = Message("enterVAT.title", Message("theCompany")),
      heading = Message("enterVAT.heading", companyName),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(EnterVATId, form(companyName), viewModel(mode, Some(companyTaskListUrl())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form(companyName).bindFromRequest().fold(
        errors =>
          Future.successful(BadRequest(view(errors, viewModel(mode, Some(companyTaskListUrl()))))),
        value =>
          for {
            _ <- cacheConnector.save(EnterVATId, value)
          } yield {
            Redirect(companydetails.routes.CheckYourAnswersController.onPageLoad())
          }
      )
  }
}
