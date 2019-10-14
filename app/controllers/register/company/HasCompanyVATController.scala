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
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasVATFormProvider
import identifiers.register.company.{BusinessDetailsId, HasCompanyVATId}
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}

import scala.concurrent.{ExecutionContext, Future}

class HasCompanyVATController @Inject()(override val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        override val dataCacheConnector: UserAnswersCacheConnector,
                                        @RegisterCompany override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: HasVATFormProvider
                                       )(implicit val ec: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.company.routes.HasCompanyVATController.onSubmit(mode),
      title = Message("hasCompanyVAT.heading", Message("theCompany").resolve),
      heading = Message("hasCompanyVAT.heading", companyName),
      hint = None
    )

  private def form(companyName: String) = formProvider("messages__hasCompanyVat__error__required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get(BusinessDetailsId) match {
          case None =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
          case Some(bd) =>
            get(HasCompanyVATId, form(bd.companyName), viewModel(mode, bd.companyName), mode, bd.companyName)
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get(BusinessDetailsId) match {
          case None =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
          case Some(bd) =>
            post(HasCompanyVATId, mode, form(bd.companyName), viewModel(mode, bd.companyName), bd.companyName)
        }
    }
}
