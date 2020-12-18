/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.partnership.routes._
import forms.AddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipPreviousAddressId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, Address}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import utils.Navigator
import utils.annotations.NoRLSCheck
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class PartnershipPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                     val cacheConnector: UserAnswersCacheConnector,
                                                     @Partnership val navigator: Navigator,
                                                     @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: AddressFormProvider,
                                                     val countryOptions: CountryOptions,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: manualAddress
                                                    )(implicit val executionContext: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = PartnershipPreviousAddressController.onSubmit _

  protected val form: Form[Address] = formProvider("error.country.invalid")

  private def viewmodel(mode: Mode, name: String)(implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    postCall(mode),
    countryOptions.options,
    title = Message("enter.previous.address.heading", Message("thePartnership")),
    heading = Message("enter.previous.address.heading", name),
    psaName = psaName()
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map { name =>
        get(viewmodel(mode, name), mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map { name =>
        post(PartnershipPreviousAddressId, viewmodel(mode, name), mode)
      }
  }

}
