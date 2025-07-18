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

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.company.directors.{DirectorAddressYearsId, DirectorNameId}
import models.requests.DataRequest
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DirectorAddressYearsController @Inject()(@CompanyDirector override val navigator: Navigator,
                                               override val cacheConnector: UserAnswersCacheConnector,
                                               override val allowAccess: AllowAccessActionProvider,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressYearsFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addressYears
                                              )(implicit val executionContext: ExecutionContext)
  extends AddressYearsController with Retrievals with I18nSupport {

  private def form(directorName: String)
                  (implicit request: DataRequest[AnyContent]): Form[AddressYears] = formProvider(directorName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        get(DirectorAddressYearsId(index), form(directorName), viewModel(mode, index, directorName, Some(companyTaskListUrl())), mode)
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        val directorName = entityName(index)
        post(DirectorAddressYearsId(index), mode, form(directorName), viewModel(mode, index, directorName, Some(companyTaskListUrl())))
    }

  private def entityName(index: Int)
                        (implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(DirectorNameId(index)).map(_.fullName).getOrElse(Message("theDirector"))

  private def viewModel(mode: Mode, index: Index, directorName: String, returnLink: Option[String])
                       (implicit request: DataRequest[AnyContent]): AddressYearsViewModel =
    AddressYearsViewModel(
      postCall = routes.DirectorAddressYearsController.onSubmit(mode, index),
      title = Message("addressYears.heading", Message("theDirector")),
      heading = Message("addressYears.heading", directorName),
      legend = Message("addressYears.heading", directorName),
      psaName = psaName(),
      returnLink = returnLink
    )

}
