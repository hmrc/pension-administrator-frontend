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

package controllers.register.administratorPartnership.partnershipDetails

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.VATNumberController
import forms.register.EnterVATFormProvider
import identifiers.register.{BusinessNameId, EnterVATId}
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipV2
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterVAT

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipEnterVATController @Inject()(
                                               val cacheConnector: UserAnswersCacheConnector,
                                               @PartnershipV2 val navigator: Navigator,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: EnterVATFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: enterVAT
                                             )(implicit val executionContext: ExecutionContext) extends VATNumberController {

  private def form(partnershipName: String)
                  (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(partnershipName)

  private def viewModel(mode: Mode, entityName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipEnterVATController.onSubmit(mode),
      title = Message("enterVAT.title", Message("thePartnership")),
      heading = Message("enterVAT.heading", entityName),
      mode = mode,
      entityName = entityName,
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(EnterVATId, form(entityName), viewModel(mode, entityName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(EnterVATId, mode, form(entityName), viewModel(mode, entityName))
  }
}
