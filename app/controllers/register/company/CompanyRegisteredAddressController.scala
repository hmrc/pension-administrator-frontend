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

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.NonUKAddressController
import forms.UKAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyAddressId
import models.requests.DataRequest
import models.{Address, Mode, RegistrationLegalStatus}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CompanyRegisteredAddressController @Inject()(
                                                    override val dataCacheConnector: UserAnswersCacheConnector,
                                                    override val registrationConnector: RegistrationConnector,
                                                    @RegisterCompany override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: UKAddressFormProvider,
                                                    val countryOptions: CountryOptions,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: nonukAddress
                                                  )(implicit val executionContext: ExecutionContext) extends NonUKAddressController with Retrievals {

  protected val form: Form[Address] = formProvider()

  protected override def createView(preparedForm: Form[?], viewModel: ManualAddressViewModel)
                                   (implicit request: Request[?], messages: Messages): () => HtmlFormat.Appendable = () =>
    view(preparedForm, viewModel)(request, messages)

  private def addressViewModel(companyName: String)(implicit request: DataRequest[AnyContent]) = ManualAddressViewModel(
    routes.CompanyRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("companyRegisteredNonUKAddress.title"),
    Message("companyRegisteredNonUKAddress.heading", companyName),
    None,
    Some(Message("companyRegisteredNonUKAddress.hintText"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        get(CompanyAddressId, addressViewModel(name))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        post(name, CompanyAddressId, addressViewModel(name), RegistrationLegalStatus.LimitedCompany)
      }
  }
}
