/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyPreviousAddressPostCodeLookupId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, RegisterCompany}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class CompanyPreviousAddressPostCodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val cacheConnector: UserAnswersCacheConnector,
                                                                override val addressLookupConnector: AddressLookupConnector,
                                                                @RegisterCompany override val navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                val view: postcodeLookup
                                                              )(implicit val executionContext: ExecutionContext) extends PostcodeLookupController {

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        get(viewModel(mode, name, Some(companyTaskListUrl())), mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        post(CompanyPreviousAddressPostCodeLookupId, viewModel(mode, name, Some(companyTaskListUrl())), mode)
      }
  }

  def viewModel(mode: Mode, name: String, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): PostcodeLookupViewModel =
    PostcodeLookupViewModel(
      routes.CompanyPreviousAddressPostCodeLookupController.onSubmit(mode),
      routes.CompanyPreviousAddressController.onPageLoad(mode),
      Message("previous.postcode.lookup.heading", Message("theCompany")),
      Message("previous.postcode.lookup.heading", name),
      Message("manual.entry.text"),
      Some(Message("manual.entry.link")),
      psaName = Some(companyName),
      returnLink = returnLink
    )

}
