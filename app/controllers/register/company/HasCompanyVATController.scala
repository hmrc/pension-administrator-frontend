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
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.company.routes.HasCompanyVATController
import forms.HasReferenceNumberFormProvider
import identifiers.register.{BusinessNameId, HasVATId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class HasCompanyVATController @Inject()(override val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        override val dataCacheConnector: UserAnswersCacheConnector,
                                        @RegisterCompany override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: HasReferenceNumberFormProvider
                                       )(implicit val ec: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = HasCompanyVATController.onSubmit(mode),
      title = Message("hasVAT.heading", Message("theCompany").resolve),
      heading = Message("hasVAT.heading", entityName),
      mode = mode,
      hint = None,
      entityName = entityName
    )

  private def companyName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany").resolve)

  private def form(companyName: String): Form[Boolean] =
    formProvider("hasVAT.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(HasVATId, form(companyName), viewModel(mode, companyName))

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        post(HasVATId, mode, form(companyName), viewModel(mode, companyName))
    }
}
