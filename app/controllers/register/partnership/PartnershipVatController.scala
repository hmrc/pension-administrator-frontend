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
import controllers.actions._
import controllers.register.VatController
import forms.register.VatFormProvider
import identifiers.register.partnership.PartnershipVatId
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{Message, VatViewModel}


class PartnershipVatController @Inject()(
                                          override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val cacheConnector: DataCacheConnector,
                                          @Partnership override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: VatFormProvider
                                        ) extends VatController {

  private def viewmodel(mode: Mode) = VatViewModel(
    postCall = routes.PartnershipVatController.onSubmit(mode),
    title = Message("partnershipVat.title"),
    heading = Message("partnershipVat.heading"),
    hint = Message("partnershipVat.hint"),
    subHeading = Some(Message("site.secondaryHeader"))
  )

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipVatId, form, viewmodel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipVatId, mode, form, viewmodel(mode))
  }
}
