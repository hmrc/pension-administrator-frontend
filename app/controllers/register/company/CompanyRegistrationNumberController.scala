/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions._
import controllers.register.EnterNumberController
import forms.register.company.CompanyRegistrationNumberFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyRegistrationNumberId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.register.company.enterNumber

import scala.concurrent.ExecutionContext

class CompanyRegistrationNumberController @Inject()(val appConfig: FrontendAppConfig,
                                                    val cacheConnector: UserAnswersCacheConnector,
                                                    @RegisterCompany val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: CompanyRegistrationNumberFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: enterNumber
                                                   )(implicit val executionContext: ExecutionContext) extends EnterNumberController {

  private val form = formProvider()

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyRegistrationNumberController.onSubmit(mode),
      title = Message("companyRegistrationNumber.heading", Message("theCompany")),
      heading = Message("companyRegistrationNumber.heading", entityName),
      hint = Some(Message("companyRegistrationNumber.hint")),
      mode = mode,
      entityName = entityName
    )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(CompanyRegistrationNumberId, form, viewModel(mode, entityName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyRegistrationNumberId, mode, form, viewModel(mode, entityName))
  }

}
