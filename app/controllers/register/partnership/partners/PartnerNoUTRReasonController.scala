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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.ReasonController
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.partnership.partners.{PartnerNameId, PartnerNoUTRReasonId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{CommonFormWithHintViewModel, Message}
import controllers.register.partnership.partners.routes.PartnerNoUTRReasonController
import scala.concurrent.ExecutionContext

class PartnerNoUTRReasonController @Inject()(
                                                @PartnershipPartner val navigator: Navigator,
                                                val appConfig: FrontendAppConfig,
                                                val messagesApi: MessagesApi,
                                                val dataCacheConnector: UserAnswersCacheConnector,
                                                authenticate: AuthAction,
                                                val allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: ReasonFormProvider
                                         )(implicit val ec: ExecutionContext) extends ReasonController {

  private def form(partnerName: String) = formProvider(partnerName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val partnerName = entityName(index)
        get(PartnerNoUTRReasonId(index), viewModel(mode, index, partnerName), form(partnerName))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partnerName = entityName(index)
      post(PartnerNoUTRReasonId(index), mode, viewModel(mode, index, partnerName), form(partnerName))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = PartnerNoUTRReasonController.onSubmit(mode, index),
      title = Message("whyNoUTR.heading", Message("thePartner").resolve),
      heading = Message("whyNoUTR.heading", partnerName),
      mode = mode,
      entityName = partnerName
    )
}