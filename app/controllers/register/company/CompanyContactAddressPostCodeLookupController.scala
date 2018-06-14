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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.company.{BusinessDetailsId, CompanyContactAddressPostCodeLookupId, CompanyDetailsId}
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
                                                               override val cacheConnector: DataCacheConnector,
                                                               override val addressLookupConnector: AddressLookupConnector,
                                                               @RegisterCompany override val navigator: Navigator,
                                                               override val messagesApi: MessagesApi,
                                                               authenticate: AuthAction,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               formProvider: PostCodeLookupFormProvider
                                                              ) extends PostcodeLookupController with Retrievals {


  def viewModel(mode: Mode): Retrieval[PostcodeLookupViewModel] = Retrieval(
    implicit request =>
      BusinessDetailsId.retrieve.right.map{ businessDetails =>
        PostcodeLookupViewModel (
          routes.CompanyContactAddressPostCodeLookupController.onSubmit(mode),
          routes.CompanyPreviousAddressController.onPageLoad(mode),
          Message("companyContactAddressPostCodeLookup.title"),
          Message("companyContactAddressPostCodeLookup.heading").withArgs(businessDetails.companyName),
          Some(Message("site.secondaryHeader")),
          Message("companyContactAddressPostCodeLookup.lede").withArgs(businessDetails.companyName),
          Message("companyContactAddressPostCodeLookup.enterPostcode"),
          Message("companyContactAddressPostCodeLookup.postalCode")
        )
      }
  )

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map(get)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode).retrieve.right.map{ vm =>
        post(CompanyContactAddressPostCodeLookupId, vm, mode)
      }
  }

}
