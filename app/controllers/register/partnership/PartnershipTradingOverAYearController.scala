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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.HasReferenceNumberController
import controllers.actions._
import controllers.register.partnership.routes._
import forms.HasReferenceNumberFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipTradingOverAYearId
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

class PartnershipTradingOverAYearController @Inject()(override val appConfig: FrontendAppConfig,
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

  private def viewModel(mode: Mode, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = PartnershipTradingOverAYearController.onSubmit(mode),
      title = Message("trading.title", Message("thePartnership")),
      heading = Message("trading.title", companyName),
      mode = mode,
      hint = None,
      entityName = companyName
    )

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider("trading.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.map {
          name =>
            get(PartnershipTradingOverAYearId, form(name), viewModel(mode, name))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.map {
          name =>
            post(PartnershipTradingOverAYearId, mode, form(name), viewModel(mode, name))
        }
    }

}
