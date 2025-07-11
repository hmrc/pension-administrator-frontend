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

package controllers.register.administratorPartnership

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.{MoreThanTenController, Retrievals}
import identifiers.register.partnership.MoreThanTenPartnersId
import models.Mode
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipPartnerV2
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MoreThanTenPartnersController @Inject()(
                                               override val cacheConnector: UserAnswersCacheConnector,
                                               @PartnershipPartnerV2 val navigator: Navigator,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: moreThanTen
                                             )(implicit val executionContext: ExecutionContext)
  extends MoreThanTenController with Retrievals {

  def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]): MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenPartners.title",
      heading = "moreThanTenPartners.heading",
      hint = "moreThanTenPartners.hint",
      postCall = routes.MoreThanTenPartnersController.onSubmit(mode),
      id = MoreThanTenPartnersId,
      psaName = psaName(),
      errorKey = "moreThanTenPartners.error.required"
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      get(viewModel(mode), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(viewModel(mode), mode)
  }
}

