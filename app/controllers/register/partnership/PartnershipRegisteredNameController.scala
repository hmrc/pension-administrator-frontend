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

package controllers.register.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.OrganisationNameController
import forms.BusinessDetailsFormModel
import identifiers.register.partnership.PartnershipDetailsId
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import utils.annotations.RegisterCompany
import viewmodels.{Message, OrganisationNameViewModel}

class PartnershipRegisteredNameController @Inject()(override val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    @Partnership override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    val cacheConnector: UserAnswersCacheConnector) extends OrganisationNameController {

  private def partnershipNameViewModel() =
    OrganisationNameViewModel(
      routes.PartnershipRegisteredNameController.onSubmit(),
      Message("partnershipName.title"),
      Message("partnershipName.heading")
    )

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipDetailsId, partnershipNameViewModel)
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipDetailsId, partnershipNameViewModel())
  }

  override protected val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "partnershipName.error.required",
      companyNameLengthMsg = "partnershipName.error.length",
      companyNameInvalidMsg = "partnershipName.error.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = None,
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )
}
