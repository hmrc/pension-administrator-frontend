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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.adviser.AdviserAddressPostCodeLookupId
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Adviser
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class AdviserAddressPostCodeLookupController @Inject()(
                                                        override val appConfig: FrontendAppConfig,
                                                        override val cacheConnector: UserAnswersCacheConnector,
                                                        override val addressLookupConnector: AddressLookupConnector,
                                                        @Adviser override val navigator: Navigator,
                                                        override val messagesApi: MessagesApi,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PostCodeLookupFormProvider
                                                      ) extends PostcodeLookupController {

  override protected def form: Form[String] = formProvider()

  import AdviserAddressPostCodeLookupController._

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserAddressPostCodeLookupId, viewModel(mode), mode)
  }
}

object AdviserAddressPostCodeLookupController {

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode),
    Message("common.adviser.address.title"),
    Message("common.adviser.address.heading"),
    Some(Message("common.adviser.secondary.heading")),
    Message("adviserAddressPostCodeLookup.hint"),
    Message("adviserAddressPostCodeLookup.enterPostcode"),
    Some(Message("adviserAddressPostCodeLookup.enterPostcode.link")),
    Message("adviserAddressPostCodeLookup.formLabel")
  )
}

