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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyContactAddressPostCodeLookupId
import models.Mode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class CompanyContactAddressPostCodeLookupController @Inject()(
                                                               override val appConfig: FrontendAppConfig,
                                                               override val cacheConnector: UserAnswersCacheConnector,
                                                               override val addressLookupConnector: AddressLookupConnector,
                                                               @RegisterCompany override val navigator: Navigator,
                                                               override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               override val allowAccess: AllowAccessActionProvider,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               formProvider: PostCodeLookupFormProvider
                                                             ) extends PostcodeLookupController with Retrievals {


  def viewModel(mode: Mode): Retrieval[PostcodeLookupViewModel] = Retrieval(
    implicit request =>
      BusinessNameId.retrieve.right.map { businessName =>
        PostcodeLookupViewModel(
          routes.CompanyContactAddressPostCodeLookupController.onSubmit(mode),
          routes.CompanyContactAddressController.onSubmit(mode),
          Message("companyContactAddressPostCodeLookup.title"),
          Message("companyContactAddressPostCodeLookup.heading").withArgs(businessName),
          Message("companyContactAddressPostCodeLookup.lede").withArgs(businessName),
          Message("common.postcodeLookup.enterPostcode"),
          Some(Message("common.postcodeLookup.enterPostcode.link")),
          Message("address.postcode"),
          psaName = psaName()
        )
      }
  )

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map{ vm =>
        get(vm, mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map { vm =>
        post(CompanyContactAddressPostCodeLookupId, vm, mode)
      }
  }

}
