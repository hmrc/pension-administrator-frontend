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

package controllers.register.partnership.partners

import connectors.cache.UserAnswersCacheConnector
import controllers.ReasonController
import controllers.actions._
import forms.NINOReasonFormProvider
import identifiers.register.partnership.partners.{PartnerNameId, PartnerNoNINOReasonId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartner}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.reason

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerNoNINOReasonController @Inject()(@PartnershipPartner val navigator: Navigator,
                                              val dataCacheConnector: UserAnswersCacheConnector,
                                              authenticate: AuthAction,
                                              @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: NINOReasonFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: reason
                                             )(implicit val executionContext: ExecutionContext) extends ReasonController with I18nSupport {

  private def form(partnerName: String)
                  (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(partnerName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val partnerName = entityName(index)
        get(PartnerNoNINOReasonId(index), viewModel(mode, index, partnerName), form(partnerName))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partnerName = entityName(index)
      post(PartnerNoNINOReasonId(index), mode, viewModel(mode, index, partnerName), form(partnerName))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerNoNINOReasonController.onSubmit(mode, index),
      title = Message("whyNoNINO.heading", Message("thePartner")),
      heading = Message("whyNoNINO.heading", partnerName),
      mode = mode,
      entityName = partnerName
    )
}
