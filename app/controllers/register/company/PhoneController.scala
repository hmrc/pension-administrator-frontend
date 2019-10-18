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
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.{Retrievals, Variations}
import forms.PhoneFormProvider
import identifiers.register.{BusinessNameId, PhoneId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

import scala.concurrent.Future

class PhoneController @Inject()(@RegisterCompany val navigator: Navigator,
                                val appConfig: FrontendAppConfig,
                                val messagesApi: MessagesApi,
                                val cacheConnector: UserAnswersCacheConnector,
                                authenticate: AuthAction,
                                val allowAccess: AllowAccessActionProvider,
                                getData: DataRetrievalAction,
                                requireData: DataRequiredAction,
                                formProvider: PhoneFormProvider
                               ) extends FrontendController with I18nSupport with Retrievals with Variations {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val filledForm =
          request.userAnswers.get(PhoneId("contactDetails")).map(form.fill).getOrElse(form)

        Future.successful(Ok(phone(appConfig, filledForm, viewModel(mode))))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(phone(appConfig, formWithErrors, viewModel(mode)))),
        contactDetails => {
          cacheConnector.save(request.externalId, PhoneId("contactDetails"), contactDetails).flatMap {
            answers =>
              saveChangeFlag(mode, PhoneId("contactDetails")).map { _ =>
                Redirect(navigator.nextPage(PhoneId("contactDetails"), mode, UserAnswers(answers)))
              }
          }
        }
      )
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany").resolve)

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.PhoneController.onSubmit(mode),
      title = Message("phone.title", Message("theCompany").resolve),
      heading = Message("phone.title", entityName),
      mode = mode,
      entityName = entityName
    )
}
