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
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyAddressYearsId
import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class CompanyAddressYearsController @Inject()(@RegisterCompany override val navigator: Navigator,
                                              override val appConfig: FrontendAppConfig,
                                              override val cacheConnector: UserAnswersCacheConnector,
                                              override val allowAccess: AllowAccessActionProvider,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: AddressYearsFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: addressYears
                                             )(implicit val executionContext: ExecutionContext, messages: Messages) extends AddressYearsController with Retrievals {

  private def form(companyName: String): Form[AddressYears] = formProvider(companyName)

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany").resolve)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(CompanyAddressYearsId, form(entityName), viewModel(mode, entityName), mode)
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(CompanyAddressYearsId, mode, form(entityName), viewModel(mode, entityName))
  }

  private def viewModel(mode: Mode, companyName: String)(implicit request: DataRequest[AnyContent]): AddressYearsViewModel = {
    AddressYearsViewModel(
      postCall = routes.CompanyAddressYearsController.onSubmit(mode),
      title = Message("addressYears.heading", Message("theCompany").resolve),
      heading = Message("addressYears.heading", companyName),
      legend = Message("addressYears.heading", companyName),
      psaName = psaName()
    )
  }
}
