/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.DataCacheConnector
import controllers.MoreThanTenController
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.partnership.MoreThanTenPartnersId
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.MoreThanTenViewModel

class MoreThanTenPartnersController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               val dataCacheConnector: DataCacheConnector,
                                               @PartnershipPartner val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction
                                             ) extends MoreThanTenController {

  import MoreThanTenPartnersController._

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      get(viewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(viewModel(mode), mode)
  }
}

object MoreThanTenPartnersController {
  def viewModel(mode: Mode): MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenPartners.title",
      heading = "moreThanTenPartners.heading",
      hint = "moreThanTenPartners.hint",
      postCall = routes.MoreThanTenPartnersController.onSubmit(mode),
      id = MoreThanTenPartnersId
    )
}
