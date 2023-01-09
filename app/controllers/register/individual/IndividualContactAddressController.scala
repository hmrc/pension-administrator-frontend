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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.individual.{IndividualContactAddressId, IndividualContactAddressListId}

import javax.inject.Inject
import models.requests.DataRequest
import models.Address
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import utils.Navigator
import utils.annotations.Individual
import utils.annotations.NoRLSCheck
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class IndividualContactAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                   val cacheConnector: UserAnswersCacheConnector,
                                                   @Individual val navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressFormProvider,
                                                   val countryOptions: CountryOptions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val view: manualAddress
                                                  )(implicit val executionContext: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.IndividualContactAddressController.onSubmit _

  protected val form: Form[Address] = formProvider("error.country.invalid")

  private def viewmodel(mode: Mode, displayReturnLink: Boolean)(implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    postCall(mode),
    countryOptions.options,
    title = Message("individual.enter.address.heading"),
    heading = Message("individual.enter.address.heading"),
    psaName = if(displayReturnLink) psaName() else None
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get( IndividualContactAddressId, IndividualContactAddressListId, viewmodel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(IndividualContactAddressId, viewmodel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty), mode)
  }
}
