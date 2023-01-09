/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.partnership.partners.{PartnerAddressYearsId, PartnerNameId}
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, PartnershipPartnerV2}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerAddressYearsController @Inject()(val appConfig: FrontendAppConfig,
                                              val cacheConnector: UserAnswersCacheConnector,
                                              @PartnershipPartnerV2 val navigator: Navigator,
                                              authenticate: AuthAction,
                                              @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: AddressYearsFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: addressYears
                                             )(implicit val executionContext: ExecutionContext)
                                               extends AddressYearsController with Retrievals with I18nSupport {


  private def form(partnerName: String)
                  (implicit request: DataRequest[AnyContent]): Form[AddressYears] = formProvider(partnerName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val partnerName = entityName(index)
        get(PartnerAddressYearsId(index), form(partnerName), viewModel(mode, index, partnerName), mode)
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val partnerName = entityName(index)
      post(PartnerAddressYearsId(index), mode, form(partnerName), viewModel(mode, index, partnerName))
  }

  private def entityName(index: Int)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]): AddressYearsViewModel =
    AddressYearsViewModel(
      postCall = routes.PartnerAddressYearsController.onSubmit(mode, index),
      title = Message("addressYears.heading", Message("thePartner")),
      heading = Message("addressYears.heading", partnerName),
      legend = Message("addressYears.heading", partnerName),
      psaName = psaName(),
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}
