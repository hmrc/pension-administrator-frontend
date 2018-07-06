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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import controllers.{ConfirmDeleteController, Retrievals}
import identifiers.register.partnership.partners.PartnerDetailsId
import javax.inject.Inject
import models.Index
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import viewmodels.{ConfirmDeleteViewModel, Message}

class ConfirmDeletePartnerController @Inject()(
                                                val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val cacheConnector: DataCacheConnector
                                              ) extends ConfirmDeleteController with Retrievals {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      PartnerDetailsId(index).retrieve.right.map{ details =>
        val viewModel = ConfirmDeleteViewModel(
          routes.ConfirmDeletePartnerController.onSubmit(index),
          routes.ConfirmDeletePartnerController.onPageLoad(index),
          Message("confirmDelete.partner.title"),
          "confirmDelete.partner.heading",
          Some(details.fullName),
          Some("site.secondaryHeader")
        )

        get(viewModel, details.isDeleted, routes.AlreadyDeletedController.onPageLoad(index))

      }
  }

  def onSubmit(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnerDetailsId(index), routes.ConfirmDeletePartnerController.onPageLoad(index))
  }

}
