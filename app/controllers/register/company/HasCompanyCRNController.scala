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

package controllers.register.company

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.company.routes._
import forms.HasReferenceNumberFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.HasCompanyCRNId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class HasCompanyCRNController @Inject()(override val appConfig: FrontendAppConfig,
                                        override val dataCacheConnector: UserAnswersCacheConnector,
                                        @RegisterCompany override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: HasReferenceNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: hasReferenceNumber
                                       )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = HasCompanyCRNController.onSubmit(mode),
      title = Message("hasCompanyNumber.heading", Message("theCompany")),
      heading = Message("hasCompanyNumber.heading", entityName),
      mode = mode,
      hint = Some(Message("hasCompanyNumber.hint")),
      entityName = entityName
    )

  private def companyName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany"))

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("companyRegistrationNumber.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(HasCompanyCRNId, form(companyName), viewModel(mode, companyName))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        post(HasCompanyCRNId, mode, form(companyName), viewModel(mode, companyName))
    }
}
