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

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.UKAddressFormProvider
import identifiers.register.company.ConfirmCompanyAddressId
import models._
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{RegisterCompany, RegisterCompanyV2}
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressController @Inject()(authenticate: AuthAction,
                                  val cacheConnector: UserAnswersCacheConnector,
                                  val controllerComponents: MessagesControllerComponents,
                                  val countryOptions: CountryOptions,
                                  val formProvider: UKAddressFormProvider,
                                  getData: DataRetrievalAction,
                                  @RegisterCompany val navigator: Navigator,
                                  @RegisterCompanyV2 val navigatorV2: Navigator,
                                  requireData: DataRequiredAction,
                                  val view: manualAddress,
                                  addressHelper: AddressHelper
                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport{

  protected val form: Form[Address] = formProvider()
  private val isUkHintText = true
  def viewModel(): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall = routes.AddressController.onSubmit(),
      countryOptions = countryOptions.options,
      title = Message("enterMissingAddressFields.title"),
      heading = Message("enterMissingAddressFields.heading")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit  request =>
      ConfirmCompanyAddressId.retrieve.map { tolerantAddress =>
        val formData = addressHelper.mapAddressFields(tolerantAddress)
        val filledForm = form.fill(tolerantAddress.toPrepopAddress)
        Future.successful(Ok(view(
          filledForm,
          viewModel(),
          mode,
          isUkHintText)
        ))
        validateForm(filledForm, formData, mode)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => Future.successful(BadRequest(view(formWithErrors, viewModel(), mode, isUkHintText))),
        address => {
          val tolerantAddress = TolerantAddress(
            Some(address.addressLine1),
            Some(address.addressLine2),
            address.addressLine3,
            address.addressLine4,
            address.postcode, Some(address.country)
          )
          for {
            _ <- cacheConnector.save(request.externalId, ConfirmCompanyAddressId, tolerantAddress)
          } yield {
              Redirect(routes.CompanyRegistrationTaskListController.onPageLoad())
          }
        }
      )
  }

  def validateForm(form: Form[Address], formData: Map[String, String], mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bind(formData).fold(
      (formWithErrors: Form[_]) => Future.successful(BadRequest(view(formWithErrors, viewModel(), mode, isUkHintText))),
      _ => Future.successful(Redirect(routes.AddressController.onSubmit()))
    )
  }
}
