/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.{BusinessNameId, HasPAYEId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class HasPartnershipPAYEController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val dataCacheConnector: UserAnswersCacheConnector,
                                             @Partnership override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: HasReferenceNumberFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: hasReferenceNumber
                                            )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, partnershipName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.partnership.routes.HasPartnershipPAYEController.onSubmit(mode),
      title = Message("hasPAYE.heading", Message("thePartnership")),
      heading = Message("hasPAYE.heading", partnershipName),
      mode = mode,
      hint = Some(Message("hasPAYE.hint")),
      entityName = partnershipName
    )

  private def form(partnershipName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider("hasPAYE.error.required", partnershipName)

  private def partnershipName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(HasPAYEId, form(partnershipName), viewModel(mode, partnershipName))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        post(HasPAYEId, mode, form(partnershipName), viewModel(mode, partnershipName))
    }
}
