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
import controllers.actions._
import controllers.register.PhoneController
import forms.PhoneFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.company.CompanyPhoneId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.{NoRLSCheck, RegisterCompany}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyPhoneController @Inject()(@RegisterCompany val navigator: Navigator,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       @NoRLSCheck val allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PhoneFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: phone
                                      )(implicit val executionContext: ExecutionContext) extends PhoneController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(CompanyPhoneId, form, viewModel(mode, Some(companyTaskListUrl())))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          Future.successful(BadRequest(view(formWithErrors, viewModel(mode, Some(companyTaskListUrl())), psaName()))),
        value => {
          for {
            _ <- cacheConnector.save(CompanyPhoneId, value)
            _ <- saveChangeFlag(mode, CompanyPhoneId)
          } yield {
            Redirect(contactdetails.routes.CheckYourAnswersController.onPageLoad())
          }
        }
      )
  }

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyPhoneController.onSubmit(mode),
      title = Message("phone.title", Message("theCompany")),
      heading = Message("phone.title", companyName),
      mode = mode,
      entityName = companyName,
      displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty,
      returnLink = returnLink
    )
}
