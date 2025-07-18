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
import controllers.actions._
import controllers.register.EnterNumberController
import forms.register.company.CompanyRegistrationNumberFormProvider
import identifiers.register.company.CompanyRegistrationNumberId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.annotations.{RegisterCompany, RegisterCompanyV2}
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.register.company.enterNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationNumberController @Inject()(
                                                     val cacheConnector: UserAnswersCacheConnector,
                                                     @RegisterCompany val navigator: Navigator,
                                                     @RegisterCompanyV2 val navigatorV2: Navigator,
                                                     authenticate: AuthAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: CompanyRegistrationNumberFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: enterNumber
                                                   )(implicit val executionContext: ExecutionContext) extends EnterNumberController with Retrievals {

  private val form = formProvider()

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyRegistrationNumberController.onSubmit(mode),
      title = Message("companyRegistrationNumber.heading", Message("theCompany")),
      heading = Message("companyRegistrationNumber.heading", companyName),
      hint = Some(Message("companyRegistrationNumber.hint")),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(CompanyRegistrationNumberId, form, viewModel(mode, Some(companyTaskListUrl())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          Future.successful(BadRequest(view(formWithErrors, viewModel(mode, Some(companyTaskListUrl()))))),
        value =>
          for {
            newCache <- cacheConnector.save(CompanyRegistrationNumberId, value)
          } yield {
            Redirect(navigatorV2.nextPage(CompanyRegistrationNumberId, mode, UserAnswers(newCache)))
          }
      )
  }

}
