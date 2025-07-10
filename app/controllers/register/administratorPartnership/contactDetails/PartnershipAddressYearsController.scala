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

package controllers.register.administratorPartnership.contactDetails

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipAddressYearsId
import models.requests.DataRequest
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipV2}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipAddressYearsController @Inject()(
                                                   val cacheConnector: UserAnswersCacheConnector,
                                                   @PartnershipV2 val navigator: Navigator,
                                                   @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressYearsFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val view: addressYears
                                                 )(implicit val executionContext: ExecutionContext)
  extends AddressYearsController with Retrievals with I18nSupport {


  private def viewModel(mode: Mode, partnershipNameValue: String)
                       (implicit request: DataRequest[AnyContent]) =
    AddressYearsViewModel(
      routes.PartnershipAddressYearsController.onSubmit(mode),
      Message("addressYears.heading", Message("thePartnership")),
      Message("addressYears.heading").withArgs(partnershipNameValue),
      Message("addressYears.heading").withArgs(partnershipNameValue),
      psaName = psaName(),
      partnershipName = Some(partnershipNameValue),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )

  def form(partnershipName: String)
          (implicit request: DataRequest[AnyContent]): Form[AddressYears] = formProvider(partnershipName)

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("thePartnership"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipAddressYearsId, form(entityName), viewModel(mode, entityName), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipAddressYearsId, mode, form(entityName), viewModel(mode, entityName))
  }
}
