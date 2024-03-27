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

package controllers.register.individual

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.individual.IndividualContactAddressPostCodeLookupId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{AuthWithIV, Individual, NoRLSCheck}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.ExecutionContext

@Singleton
class IndividualContactAddressPostCodeLookupController @Inject()(
                                                                  @Individual override val navigator: Navigator,
                                                                  override val appConfig: FrontendAppConfig,
                                                                  override val cacheConnector: UserAnswersCacheConnector,
                                                                  override val addressLookupConnector: AddressLookupConnector,
                                                                  @AuthWithIV authenticate: AuthAction,
                                                                  @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  formProvider: PostCodeLookupFormProvider,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  val view: postcodeLookup
                                                                )(implicit val executionContext: ExecutionContext
                                                                ) extends PostcodeLookupController {

  def viewModel(mode: Mode, displayReturnLink: Boolean)(implicit request: DataRequest[AnyContent]) = PostcodeLookupViewModel(
    routes.IndividualContactAddressPostCodeLookupController.onSubmit(mode),
    routes.IndividualContactAddressController.onPageLoad(mode),
    Message("individual.postcode.lookup.heading"),
    Message("individual.postcode.lookup.heading"),
    Message("manual.entry.text"),
    Some(Message("manual.entry.link")),
    psaName = if(displayReturnLink) psaName() else None,
    findAddressMessageKey = "site.save_and_continue"
  )

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(IndividualContactAddressPostCodeLookupId, viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty), mode)
  }
}


