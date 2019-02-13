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

package controllers.register.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.BusinessDetailsController
import controllers.actions._
import forms.BusinessDetailsFormModel
import identifiers.register.company.BusinessDetailsId
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.{BusinessDetailsViewModel, Message}

class CompanyBusinessDetailsController @Inject()(val appConfig: FrontendAppConfig,
                                                 val messagesApi: MessagesApi,
                                                 val dataCacheConnector: UserAnswersCacheConnector,
                                                 @RegisterCompany val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction
                                                ) extends BusinessDetailsController {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      get(BusinessDetailsId)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(BusinessDetailsId)
  }

  // scalastyle:off magic.number
  override protected val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "businessDetails.error.companyName.required",
      companyNameLengthMsg = "businessDetails.error.companyName.length",
      companyNameInvalidMsg = "businessDetails.error.companyName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = Some("businessDetails.error.utr.required"),
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )
  // scalastyle:on magic.number

  override protected lazy val viewModel: BusinessDetailsViewModel = BusinessDetailsViewModel(
    postCall = routes.CompanyBusinessDetailsController.onSubmit(),
    title = Message("businessDetails.title"),
    heading = Message("businessDetails.heading"),
    companyNameLabel = Message("businessDetails.companyName"),
    companyNameHint = Message("businessDetails.companyName.hint"),
    utrLabel = Message("businessDetails.utr"),
    utrHint = Message("businessDetails.utr.hint")
  )

}
