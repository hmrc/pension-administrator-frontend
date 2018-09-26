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
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.company.CompanyPreviousAddressPostCodeLookupId
import models.Mode
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class CompanyPreviousAddressPostCodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val cacheConnector: UserAnswersCacheConnector,
                                                                override val addressLookupConnector: AddressLookupConnector,
                                                                @RegisterCompany override val navigator: Navigator,
                                                                override val messagesApi: MessagesApi,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider
                                                              ) extends PostcodeLookupController {

  import CompanyPreviousAddressPostCodeLookupController._

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyPreviousAddressPostCodeLookupId, viewModel(mode), mode)
  }

}

object CompanyPreviousAddressPostCodeLookupController {

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    routes.CompanyPreviousAddressPostCodeLookupController.onSubmit(mode),
    routes.CompanyPreviousAddressController.onPageLoad(mode),
    Message("companyPreviousAddressPostCodeLookup.title"),
    Message("companyPreviousAddressPostCodeLookup.heading"),
    Some(Message("site.secondaryHeader")),
    Message("companyPreviousAddressPostCodeLookup.lede"),
    Message("companyPreviousAddressPostCodeLookup.enterPostcode"),
    Some(Message("companyPreviousAddressPostCodeLookup.enterPostcode.link")),
    Message("companyPreviousAddressPostCodeLookup.postalCode")
  )

}
