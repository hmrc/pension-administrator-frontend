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
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.VATNumberController
import forms.register.company.VATNumberFormProvider
import identifiers.register.VATNumberId
import identifiers.register.company.BusinessDetailsId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.ExecutionContext

class CompanyVATNumberController @Inject()(val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           @RegisterCompany val navigator: Navigator,
                                           authenticate: AuthAction,
                                           allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: VATNumberFormProvider
                                          )(implicit val ec: ExecutionContext) extends VATNumberController {

  private val form = formProvider()

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyVATNumberController.onSubmit(mode),
      title = Message("VATNumber.title", Message("theCompany").resolve),
      heading = Message("VATNumber.heading", entityName),
      mode = mode,
      entityName = entityName
    )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessDetailsId).fold(Message("theCompany").resolve)(_.companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(VATNumberId, form, viewModel(mode, entityName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(VATNumberId, mode, form, viewModel(mode, entityName))
  }

}
