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

package controllers.register.administratorPartnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.{BusinessNameId, HasVATId}
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipV2
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class HasPartnershipVATController @Inject()(override val appConfig: FrontendAppConfig,
                                            override val dataCacheConnector: UserAnswersCacheConnector,
                                            @PartnershipV2 val navigator: Navigator,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: HasReferenceNumberFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val view: hasReferenceNumber
                                           )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController with I18nSupport {

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.HasPartnershipVATController.onSubmit(mode),
      title = Message("hasVAT.heading", Message("thePartnership")),
      heading = Message("hasVAT.heading", entityName),
      mode = mode,
      hint = None,
      entityName = entityName,
      displayPartnershipLink = true
    )

  private def partnershipName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))

  private def form(partnershipName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("hasVAT.error.required", partnershipName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(HasVATId, form(partnershipName), viewModel(mode, partnershipName))

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        post(HasVATId, mode, form(partnershipName), viewModel(mode, partnershipName))
    }
}
