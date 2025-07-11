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

package controllers.register.adviser

import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser.{AdviserAddressPostCodeLookupId, AdviserNameId}
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{Adviser, NoRLSCheck}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdviserAddressPostCodeLookupController @Inject()(
                                                        override val cacheConnector: UserAnswersCacheConnector,
                                                        override val addressLookupConnector: AddressLookupConnector,
                                                        @Adviser override val navigator: Navigator,
                                                        @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PostCodeLookupFormProvider,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: postcodeLookup
                                                      )(implicit val executionContext: ExecutionContext) extends PostcodeLookupController {

  override protected def form: Form[String] = formProvider()

  def viewModel(mode: Mode, displayReturnLink: Boolean, returnLink: Option[String])
               (implicit request: DataRequest[AnyContent]): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode),
    Message("postcode.lookup.heading", Message("theAdviser")),
    Message("postcode.lookup.heading", entityName),
    Message("manual.entry.text"),
    Some(Message("manual.entry.link")),
    psaName = if(displayReturnLink) psaName() else None,
    returnLink = returnLink
  )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl())), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserAddressPostCodeLookupId, viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl())), mode)
  }
}


