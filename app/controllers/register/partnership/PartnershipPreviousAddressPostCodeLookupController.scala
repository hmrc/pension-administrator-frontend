/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipPreviousAddressPostCodeLookupId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

@Singleton
class PartnershipPreviousAddressPostCodeLookupController @Inject()(
                                                                    @Partnership override val navigator: Navigator,
                                                                    override val appConfig: FrontendAppConfig,
                                                                    override val cacheConnector: UserAnswersCacheConnector,
                                                                    override val addressLookupConnector: AddressLookupConnector,
                                                                    override val messagesApi: MessagesApi,
                                                                    override val allowAccess: AllowAccessActionProvider,
                                                                    authenticate: AuthAction,
                                                                    getData: DataRetrievalAction,
                                                                    requireData: DataRequiredAction,
                                                                    formProvider: PostCodeLookupFormProvider
                                                                  ) extends PostcodeLookupController with Retrievals {

  def viewModel(mode: Mode, name: String)(implicit request: DataRequest[AnyContent]) = PostcodeLookupViewModel(
    routes.PartnershipPreviousAddressPostCodeLookupController.onSubmit(mode),
    routes.PartnershipPreviousAddressController.onPageLoad(mode),
    Message("previousAddressPostCode.heading", Message("thePartnership").resolve),
    Message("previousAddressPostCode.heading", name),
    Message("common.previousAddress.enterPostcode"),
    Some(Message("common.previousAddress.enterPostcode.link")),
    Message("common.address.enterPostcode.formLabel"),
    psaName()
  )

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map {
        name =>
          get(viewModel(mode, name), mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map {
        name =>
          post(PartnershipPreviousAddressPostCodeLookupId, viewModel(mode, name), mode)
      }
  }
}


