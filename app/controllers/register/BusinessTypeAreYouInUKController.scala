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

package controllers.register

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.*
import forms.register.YesNoFormProvider
import identifiers.register.AreYouInUKId
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{AuthWithNoIV, Register}
import utils.{Navigator, UserAnswers}
import viewmodels.{AreYouInUKViewModel, Message}
import views.html.register.areYouInUK

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessTypeAreYouInUKController @Inject()(
                                                  dataCacheConnector: UserAnswersCacheConnector,
                                                  @Register val navigator: Navigator,
                                                  allowAccess: AllowAccessActionProvider,
                                                  @AuthWithNoIV val authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  formProvider: YesNoFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: areYouInUK
                                                )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider("business.areYouInUK.error.required")

  protected def viewModel(mode: Mode) =
    AreYouInUKViewModel(mode,
      postCall = controllers.register.routes.BusinessTypeAreYouInUKController.onSubmit(mode),
      title = Message("areYouInUK.title"),
      heading = Message("areYouInUK.heading"),
      p1 = Some("areYouInUK.check.selectedUkAddress"),
      p2 = Some("areYouInUK.check.provideNonUkAddress")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers match {
        case None => form
        case Some(userAnswers) =>
          userAnswers.get(AreYouInUKId).fold(form)(v => form.fill(v))
      }
      Ok(view(preparedForm, viewModel(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          Future.successful(BadRequest(view(formWithErrors, viewModel(mode)))),
        value => {
          dataCacheConnector.save(AreYouInUKId, value).map(cacheMap =>
            Redirect(navigator.nextPage(AreYouInUKId, mode, UserAnswers(cacheMap))))
        })
  }
}
