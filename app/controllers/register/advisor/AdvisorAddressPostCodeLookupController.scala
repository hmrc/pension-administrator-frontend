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

package controllers.register.advisor

import javax.inject.Inject

import play.api.i18n.MessagesApi
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.advisor.AdvisorAddressPostCodeLookupId
import models.Mode
import play.api.data.Form
import utils.Navigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class AdvisorAddressPostCodeLookupController @Inject()(
                                                        override val appConfig: FrontendAppConfig,
                                                        override val cacheConnector: DataCacheConnector,
                                                        override val addressLookupConnector: AddressLookupConnector,
                                                        override val navigator: Navigator,
                                                        override val messagesApi: MessagesApi,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PostCodeLookupFormProvider
                                                      ) extends PostcodeLookupController {

  override protected def form: Form[String] = formProvider()

  import AdvisorAddressPostCodeLookupController._

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdvisorAddressPostCodeLookupId, viewModel(mode), mode)
  }
}

object AdvisorAddressPostCodeLookupController {

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.advisor.routes.AdvisorAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.advisor.routes.AdvisorAddressPostCodeLookupController.onPageLoad(mode),
    Message("advisorAddressPostCodeLookup.title"),
    Message("advisorAddressPostCodeLookup.heading"),
    Some(Message("common.advisor.secondary.heading")),
    Message("advisorAddressPostCodeLookup.hint"),
    Message("advisorAddressPostCodeLookup.enterPostcode"),
    Message("advisorAddressPostCodeLookup.formLabel"),
    Message("advisorAddressPostCodeLookup.formHint")
  )
}

