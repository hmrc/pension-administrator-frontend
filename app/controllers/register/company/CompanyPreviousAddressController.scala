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

package controllers.register.company

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.UKAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.{CompanyAddressListId, CompanyPreviousAddressId}
import models.FeatureToggleName.PsaRegistration
import models.requests.DataRequest
import models.{Address, Mode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.NoRLSCheck
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class CompanyPreviousAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                                 override val cacheConnector: UserAnswersCacheConnector,
                                                 @RegisterCompany override val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 @NoRLSCheck override val allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: UKAddressFormProvider,
                                                 val countryOptions: CountryOptions,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: manualAddress,
                                                 featureToggleConnector: FeatureToggleConnector
                                                )(implicit val executionContext: ExecutionContext) extends ManualAddressController {

  override protected val form: Form[Address] = formProvider("error.country.invalid")

  private def addressViewModel(mode: Mode, name: String, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    routes.CompanyPreviousAddressController.onSubmit(mode),
    countryOptions.options,
    title = Message("enter.previous.address.heading", Message("theCompany")),
    heading = Message("enter.previous.address.heading", name),
    psaName = Some(companyName),
    returnLink = returnLink
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
          val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
          get(CompanyPreviousAddressId, CompanyAddressListId, addressViewModel(mode, name, returnLink), mode)
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
          val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
          post(CompanyPreviousAddressId, addressViewModel(mode, name, returnLink), mode)
        }
      }
  }

}
