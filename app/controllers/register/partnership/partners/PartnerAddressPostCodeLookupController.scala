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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.partnership.partners.{PartnerAddressPostCodeLookupId, PartnerDetailsId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.Future

class PartnerAddressPostCodeLookupController @Inject()(
                                                        override val appConfig: FrontendAppConfig,
                                                        override val cacheConnector: DataCacheConnector,
                                                        override val addressLookupConnector: AddressLookupConnector,
                                                        @PartnershipPartner override val navigator: Navigator,
                                                        override val messagesApi: MessagesApi,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PostCodeLookupFormProvider
                                                      ) extends PostcodeLookupController with Retrievals {

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(
        viewModel =>
          post(PartnerAddressPostCodeLookupId(index), viewModel, mode)
      )
  }

  private def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], PostcodeLookupViewModel] = {
    PartnerDetailsId(index).retrieve.right.map {
      partner =>
        PostcodeLookupViewModel(
          routes.PartnerAddressPostCodeLookupController.onSubmit(mode, index),
          routes.PartnerAddressController.onPageLoad(mode, index),
          Message("partnerAddressPostCodeLookup.title"),
          Message("partnerAddressPostCodeLookup.heading"),
          Some(Message(partner.fullName)),
          Message("partnerAddressPostCodeLookup.body"),
          Message("common.postcodeLookup.enterPostcode"),
          Some(Message("common.postcodeLookup.enterPostcode.link")),
          Message("common.address.enterPostcode.formLabel")
        )
    }
  }

}
