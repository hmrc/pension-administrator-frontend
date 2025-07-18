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

import audit.AuditService
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId, AdviserNameId}
import models.requests.DataRequest
import models.{Address, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{Adviser, NoRLSCheck}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdviserAddressController @Inject()(
                                         override val cacheConnector: UserAnswersCacheConnector,
                                         @Adviser override val navigator: Navigator,
                                         @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddressFormProvider,
                                         val countryOptions: CountryOptions,
                                         val auditService: AuditService,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: manualAddress
                                        )(implicit val executionContext: ExecutionContext) extends ManualAddressController {

  protected val form: Form[Address] = formProvider()
  private val isUkHintText = false
  private def addressViewModel(mode: Mode, displayReturnLink: Boolean, returnLink: Option[String])
                              (implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    routes.AdviserAddressController.onSubmit(mode),
    countryOptions.options,
    Message("enter.address.heading", Message("theAdviser")),
    Message("enter.address.heading", entityName),
    psaName = if (displayReturnLink) psaName() else None,
    returnLink = returnLink
  )

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(AdviserAddressId, AdviserAddressListId, addressViewModel(mode,
        request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl())), mode, isUkHintText)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserAddressId, addressViewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty, Some(companyTaskListUrl())), mode, isUkHintText)
  }
}
