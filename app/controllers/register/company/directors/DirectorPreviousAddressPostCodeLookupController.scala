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

package controllers.register.company.directors

import com.google.inject.Inject
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.company.directors.DirectorPreviousAddressPostCodeLookupId
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

class DirectorPreviousAddressPostCodeLookupController @Inject()(
                                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                                 override val addressLookupConnector: AddressLookupConnector,
                                                                 @CompanyDirector override val navigator: Navigator,
                                                                 override val allowAccess: AllowAccessActionProvider,
                                                                 authenticate: AuthAction,
                                                                 getData: DataRetrievalAction,
                                                                 requireData: DataRequiredAction,
                                                                 formProvider: PostCodeLookupFormProvider,
                                                                 val controllerComponents: MessagesControllerComponents,
                                                                 val view: postcodeLookup
                                                               )(implicit val executionContext: ExecutionContext)
                                                                 extends PostcodeLookupController with Retrievals {

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(mode, index) {
        directorName =>
          get(viewModel(mode, index, directorName, Some(companyTaskListUrl())), mode)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(mode, index) {
        directorName =>
          post(DirectorPreviousAddressPostCodeLookupId(index), viewModel(mode, index, directorName, Some(companyTaskListUrl())), mode)
      }
  }

  private def viewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])
                       (implicit request: DataRequest[AnyContent]): PostcodeLookupViewModel = {
    PostcodeLookupViewModel(
      routes.DirectorPreviousAddressPostCodeLookupController.onSubmit(mode, index),
      routes.DirectorPreviousAddressController.onPageLoad(mode, index),
      Message("previous.postcode.lookup.heading", Message("theDirector")),
      Message("previous.postcode.lookup.heading", directorName),
      Message("manual.entry.text"),
      Some(Message("manual.entry.link")),
      psaName = psaName(),
      returnLink = returnLink
    )
  }

}
