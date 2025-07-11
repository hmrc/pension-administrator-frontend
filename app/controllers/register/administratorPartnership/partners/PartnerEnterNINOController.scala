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

package controllers.register.administratorPartnership.partners

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.NINOController
import forms.register.NINOFormProvider
import identifiers.register.partnership.partners.{PartnerEnterNINOId, PartnerNameId}
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartnerV2}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterNINO

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerEnterNINOController @Inject()(@PartnershipPartnerV2 val navigator: Navigator,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           authenticate: AuthAction,
                                           @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: NINOFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: enterNINO
                                          )(implicit val executionContext: ExecutionContext)
  extends NINOController with I18nSupport {

  private def form(partnerName: String)
                  (implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(partnerName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val partnerName = entityName(index)
        get(PartnerEnterNINOId(index), form(partnerName), viewModel(mode, index, partnerName))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partnerName = entityName(index)
      post(PartnerEnterNINOId(index), mode, form(partnerName), viewModel(mode, index, partnerName))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerEnterNINOController.onSubmit(mode, index),
      title = Message("enterNINO.heading", Message("thePartner")),
      heading = Message("enterNINO.heading", partnerName),
      hint = Some(Message("enterNINO.hint")),
      mode = mode,
      entityName = psaName().getOrElse(Message("thePartnership")),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}
