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

package controllers.register.partnership

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.NonUKAddressController
import forms.address.NonUKAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipRegisteredAddressId
import models.{Address, Mode, RegistrationLegalStatus}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import utils.Navigator
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipRegisteredAddressController @Inject()(
                                                        override val dataCacheConnector: UserAnswersCacheConnector,
                                                        override val registrationConnector: RegistrationConnector,
                                                        @Partnership override val navigator: Navigator,
                                                        authenticate: AuthAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: NonUKAddressFormProvider,
                                                        val countryOptions: CountryOptions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: nonukAddress
                                                      )(implicit val executionContext: ExecutionContext)
  extends NonUKAddressController with Retrievals {

  protected val form: Form[Address] = formProvider()

  protected override def createView(preparedForm: Form[?], viewModel: ManualAddressViewModel)(
    implicit request: Request[?], messages: Messages): () => HtmlFormat.Appendable = () =>
    view(preparedForm, viewModel)(request, messages)

  private def addressViewModel(partnershipName: String)(implicit messages: Messages) = ManualAddressViewModel(
    routes.PartnershipRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("partnershipRegisteredNonUKAddress.title"),
    Message("partnershipRegisteredNonUKAddress.heading", partnershipName),
    None,
    Some(Message("partnershipRegisteredNonUKAddress.hintText"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        get(PartnershipRegisteredAddressId, addressViewModel(name))
      }
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { name =>
        post(name, PartnershipRegisteredAddressId, addressViewModel(name), RegistrationLegalStatus.Partnership)
      }
  }
}
