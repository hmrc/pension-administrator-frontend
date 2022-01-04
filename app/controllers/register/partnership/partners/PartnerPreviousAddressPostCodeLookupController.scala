/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.partnership.partners.{PartnerNameId, PartnerPreviousAddressPostCodeLookupId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartner}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class PartnerPreviousAddressPostCodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val cacheConnector: UserAnswersCacheConnector,
                                                                override val addressLookupConnector: AddressLookupConnector,
                                                                @PartnershipPartner override val navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                val view: postcodeLookup
                                                              )(implicit val executionContext: ExecutionContext
                                                              ) extends PostcodeLookupController with Retrievals {

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.map { pn =>
        get(viewModel(mode, index, pn.fullName), mode)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.map { pn =>
        post(PartnerPreviousAddressPostCodeLookupId(index), viewModel(mode, index, pn.fullName), mode)
      }
  }

  private def viewModel(mode: Mode, index: Index, name:String)(implicit request: DataRequest[AnyContent]): PostcodeLookupViewModel =
        PostcodeLookupViewModel(
          routes.PartnerPreviousAddressPostCodeLookupController.onSubmit(mode, index),
          routes.PartnerPreviousAddressController.onPageLoad(mode, index),
          Message("previous.postcode.lookup.heading", Message("thePartner")),
          Message("previous.postcode.lookup.heading", name),
          Message("manual.entry.text"),
          Some(Message("manual.entry.link")),
          Message("postcode.lookup.form.label"),
          psaName = psaName()
        )

}
