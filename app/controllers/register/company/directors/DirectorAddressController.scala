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

package controllers.register.company.directors

import com.google.inject.Inject
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import controllers.{Retrievals, Variations}
import forms.AddressFormProvider
import identifiers.register.company.directors.{CompanyDirectorAddressListId, DirectorAddressId}
import models.requests.DataRequest
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class DirectorAddressController @Inject()(
                                          override val cacheConnector: UserAnswersCacheConnector,
                                          @CompanyDirector override val navigator: Navigator,
                                          override val allowAccess: AllowAccessActionProvider,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AddressFormProvider,
                                          countryOptions: CountryOptions,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: manualAddress
                                         )(implicit val executionContext: ExecutionContext) extends ManualAddressController with Retrievals with Variations {

  override protected val form: Form[Address] = formProvider()
  private val isUkHintText = false
  private def addressViewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])
                              (implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    routes.DirectorAddressController.onSubmit(mode, index),
    countryOptions.options,
    Message("enter.address.heading", Message("theDirector")),
    Message("enter.address.heading", directorName),
    psaName = psaName(),
    returnLink = returnLink
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(mode, index) {
        directorName =>
          get(DirectorAddressId(index), CompanyDirectorAddressListId(index),
            addressViewModel(mode, index, directorName, Some(companyTaskListUrl())), mode, isUkHintText)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(mode, index) {
        directorName =>
          post(DirectorAddressId(index), addressViewModel(mode, index, directorName, Some(companyTaskListUrl())), mode, isUkHintText)
      }
  }

}
