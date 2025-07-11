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
import controllers.{HasReferenceNumberController, Retrievals}
import forms.HasReferenceNumberFormProvider
import identifiers.register.HasPAYEId
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.annotations.{RegisterCompany, RegisterCompanyV2}
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasCompanyPAYEController @Inject()(
                                         override val dataCacheConnector: UserAnswersCacheConnector,
                                         @RegisterCompany override val navigator: Navigator,
                                         @RegisterCompanyV2 val navigatorV2: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: HasReferenceNumberFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: hasReferenceNumber
                                        )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController with Retrievals {

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.company.routes.HasCompanyPAYEController.onSubmit(mode),
      title = Message("hasPAYE.heading", Message("theCompany")),
      heading = Message("hasPAYE.heading", companyName),
      mode = mode,
      hint = Some(Message("hasPAYE.hint")),
      entityName = companyName,
      returnLink = returnLink
    )

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider("hasPAYE.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(HasPAYEId, form(companyName), viewModel(mode, Some(companyTaskListUrl())))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        form(companyName).bindFromRequest().fold(
          (formWithErrors: Form[?]) =>
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode, Some(companyTaskListUrl()))))),
          value =>
            for {
              newCache <- dataCacheConnector.save(HasPAYEId, value)
            } yield {
              if (!value) {
                Redirect(navigatorV2.nextPage(HasPAYEId, mode, UserAnswers(newCache)))
              } else {
                Redirect(navigator.nextPage(HasPAYEId, mode, UserAnswers(newCache)))
              }
            }
        )
    }
}
