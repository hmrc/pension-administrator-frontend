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

package controllers.register.company.directors

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.company.directors.{DirectorDetailsId, DirectorPreviousAddressPostCodeLookupId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.Future

class DirectorPreviousAddressPostCodeLookupController @Inject()(
                                                                 override val appConfig: FrontendAppConfig,
                                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                                 override val addressLookupConnector: AddressLookupConnector,
                                                                 @CompanyDirector override val navigator: Navigator,
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
          post(DirectorPreviousAddressPostCodeLookupId(index), viewModel, mode)
      )
  }

  private def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], PostcodeLookupViewModel] = {
    DirectorDetailsId(index).retrieve.right.map {
      director =>
        PostcodeLookupViewModel(
          routes.DirectorPreviousAddressPostCodeLookupController.onSubmit(mode, index),
          routes.DirectorPreviousAddressController.onPageLoad(mode, index),
          Message("directorPreviousAddressPostCodeLookup.title"),
          Message("directorPreviousAddressPostCodeLookup.heading"),
          Some(Message(director.fullName)),
          Message("directorPreviousAddressPostCodeLookup.text"),
          Message("directorPreviousAddressPostCodeLookup.enterPostcode"),
          Some(Message("directorPreviousAddressPostCodeLookup.enterPostcode.link")),
          Message("directorPreviousAddressPostCodeLookup.input.text")
        )
    }
  }

}
