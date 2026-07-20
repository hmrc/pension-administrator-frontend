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
import connectors.cache.UserAnswersCacheConnector
import controllers.{Retrievals, Variations}
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.UKOnlyAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.{CompanyUKContactAddressId, CompanyContactAddressListId}
import models.{AddressUKOnly, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.{Navigator, UserAnswers}
import utils.annotations.{NoRLSCheck, RegisterCompany}
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddressUKOnly
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class CompanyContactAddressController @Inject()(
                                                 val cacheConnector: UserAnswersCacheConnector,
                                                 @RegisterCompany val nav: Navigator,
                                                 authenticate: AuthAction,
                                                 @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: UKOnlyAddressFormProvider,
                                                 val countryOptions: CountryOptions,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: manualAddressUKOnly
                                               )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals with Variations {

  val form: Form[AddressUKOnly] = formProvider()

  private def viewModel(mode: Mode, returnLink: Option[String]): Retrieval[ManualAddressViewModel] =
    Retrieval(
      implicit request =>
        BusinessNameId.retrieve.map { companyName =>
          ManualAddressViewModel(
            routes.CompanyContactAddressController.onSubmit(mode),
            countryOptions.options,
            Message("enter.address.heading", Message("theCompany")),
            Message("enter.address.heading", companyName),
            psaName = psaName(),
            returnLink = returnLink
          )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, Some(companyTaskListUrl())).retrieve.map(
        vm =>
          val preparedForm = request.userAnswers.get(CompanyUKContactAddressId) match {
            case None => request.userAnswers.get(CompanyContactAddressListId) match {
              case Some(value) => form.fill(AddressUKOnly.fromTolerant(value))
              case None => form
            }
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, vm, mode)))
      )
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, Some(companyTaskListUrl())).retrieve.map(
        vm =>
          form.bindFromRequest().fold(
            (formWithError: Form[?]) => Future.successful(BadRequest(view(formWithError, vm, mode))),
            address => {
              cacheConnector.save(CompanyUKContactAddressId, address).flatMap { userAnswersJson =>
                saveChangeFlag(mode, CompanyUKContactAddressId)
                  .flatMap {
                    _ =>
                      Future.successful(Redirect(nav.nextPage(CompanyUKContactAddressId, mode, UserAnswers(userAnswersJson))))
                  }
              }
            }
          )
      )
  }
}
