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

package controllers.register.individual

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.individual.routes._
import forms.AddressFormProvider
import identifiers.register.individual.{IndividualPreviousAddressId, IndividualPreviousAddressListId, IndividualPreviousAddressPostCodeLookupId}
import javax.inject.Inject
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Individual
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class IndividualPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                    val messagesApi: MessagesApi,
                                                    val cacheConnector: UserAnswersCacheConnector,
                                                    @Individual val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    override val allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: AddressFormProvider,
                                                    val countryOptions: CountryOptions,
                                                    val auditService: AuditService
                                                   ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = IndividualPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "common.previousAddress.title"
  private[controllers] val heading: Message = "common.previousAddress.heading"
  private[controllers] val hint: Message = "common.previousAddress.lede"

  protected val form: Form[Address] = formProvider("error.country.invalid")

  private def viewmodel(mode: Mode) = ManualAddressViewModel(
    postCall(mode),
    countryOptions.options,
    title = Message(title),
    heading = Message(heading),
    hint = Some(Message(hint)),
    secondaryHeader = None
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(IndividualPreviousAddressId, IndividualPreviousAddressListId, viewmodel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(IndividualPreviousAddressId, IndividualPreviousAddressListId, viewmodel(mode), mode, "Individual Previous Address",
        IndividualPreviousAddressPostCodeLookupId)
  }

}
