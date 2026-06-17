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

package controllers.register.individual

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.*
import forms.register.YesNoFormProvider
import identifiers.register.AreYouInUKId
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.AuthWithNoIV
import utils.navigators.IndividualNavigatorV2
import viewmodels.{AreYouInUKViewModel, Message}
import views.html.register.areYouInUK

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualAreYouInUKController @Inject()(
                                                dataCacheConnector: UserAnswersCacheConnector,
                                                val navigatorV2: IndividualNavigatorV2,
                                                allowAccess: AllowAccessActionProvider,
                                                @AuthWithNoIV val authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                formProvider: YesNoFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: areYouInUK
                                              )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider("areYouInUKIndividual.error.required")

  private def viewModel(mode: Mode): AreYouInUKViewModel =
    AreYouInUKViewModel(mode,
      postCall = controllers.register.individual.routes.IndividualAreYouInUKController.onSubmit(mode),
      title = Message("areYouInUKIndividual.title"),
      heading = Message("areYouInUKIndividual.heading"),
      p1 = Some("areYouInUKIndividual.check.selectedUkAddress"),
      p2 = Some("areYouInUKIndividual.check.provideNonUkAddress")
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

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async { implicit request =>
    form.bindFromRequest().fold((formWithErrors: Form[?]) =>
      Future.successful(BadRequest(view(formWithErrors, viewModel(mode)))),
      value => {
        if (!value) {
          dataCacheConnector.save(AreYouInUKId, value)
            .map(_ => Redirect(controllers.register.individual.routes.NonUKAdministratorController.onPageLoad()))
        } else {
          dataCacheConnector.save(AreYouInUKId, value).map(cacheMap =>
            Redirect(navigatorV2.nextPage(AreYouInUKId, mode, UserAnswers(cacheMap)))
          )
        }
      }
    )
  }

}
