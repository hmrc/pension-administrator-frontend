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
import connectors.{DataCacheConnector, PSANameCacheConnector}
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.partnership.PartnershipContactDetailsId
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{ContactDetailsViewModel, Message}

class PartnershipContactDetailsController @Inject()(
                                                     @Partnership override val navigator: Navigator,
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val cacheConnector: DataCacheConnector,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: ContactDetailsFormProvider,
                                                     override val psaNameCacheConnector: PSANameCacheConnector
                                                   ) extends controllers.ContactDetailsController {

  private def viewmodel(mode: Mode) = ContactDetailsViewModel(
    postCall = routes.PartnershipContactDetailsController.onSubmit(mode),
    title = Message("partnershipContactDetails.title"),
    heading = Message("partnershipContactDetails.heading"),
    body = Some(Message("contactDetails.body")),
    Some(Message("site.secondaryHeader"))
  )

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipContactDetailsId, form, viewmodel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipContactDetailsId, mode, form, viewmodel(mode), savePsaEmail = true)
  }
}
