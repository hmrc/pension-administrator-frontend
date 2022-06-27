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

package controllers.register.administratorPartnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.EmailAddressController
import forms.EmailFormProvider
import identifiers.register.partnership.partners.{PartnerEmailId, PartnerNameId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartnerV2}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerEmailController @Inject()(@PartnershipPartnerV2 val navigator: Navigator,
                                       val appConfig: FrontendAppConfig,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: EmailFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: email
                                      )(implicit val executionContext: ExecutionContext) extends EmailAddressController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(PartnerEmailId(index), form, viewModel(mode, index, entityName(index)))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnerEmailId(index), mode, form, viewModel(mode, index, entityName(index)))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerEmailController.onSubmit(mode, index),
      title = Message("email.title", Message("thePartner")),
      heading = Message("email.title", partnerName),
      mode = mode,
      entityName = psaName().getOrElse(Message("thePartnership")),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}
