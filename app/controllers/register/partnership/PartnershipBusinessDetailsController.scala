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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.BusinessDetailsController
import controllers.actions._
import forms.BusinessDetailsFormModel
import identifiers.register.partnership.PartnershipDetailsId
import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{BusinessDetailsViewModel, Message}

class PartnershipBusinessDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                     val messagesApi: MessagesApi,
                                                     val dataCacheConnector: UserAnswersCacheConnector,
                                                     @Partnership val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction
                                                    ) extends BusinessDetailsController {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      get(PartnershipDetailsId)
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipDetailsId)
  }

  // scalastyle:off magic.number
  override protected val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "partnershipBusinessDetails.error.partnershipName.required",
      companyNameLengthMsg = "partnershipBusinessDetails.error.partnershipName.length",
      companyNameInvalidMsg = "partnershipBusinessDetails.error.partnershipName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = Some("partnershipBusinessDetails.error.utr.required"),
      utrLengthMsg = "partnershipBusinessDetails.error.utr.length",
      utrInvalidMsg = "partnershipBusinessDetails.error.utr.invalid"
    )
  // scalastyle:on magic.number

  override protected lazy val viewModel: BusinessDetailsViewModel =
    BusinessDetailsViewModel(
      postCall = routes.PartnershipBusinessDetailsController.onSubmit(),
      title = Message("partnershipBusinessDetails.title"),
      heading = Message("partnershipBusinessDetails.heading"),
      companyNameLabel = Message("partnershipBusinessDetails.partnershipName"),
      companyNameHint = Message("partnershipBusinessDetails.partnershipName.hint"),
      utrLabel = Message("partnershipBusinessDetails.utr"),
      utrHint = Message("partnershipBusinessDetails.utr.hint")
    )

}
