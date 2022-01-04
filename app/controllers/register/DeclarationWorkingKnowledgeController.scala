/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions._
import forms.register.DeclarationWorkingKnowledgeFormProvider
import identifiers.register.DeclarationWorkingKnowledgeId
import models.Mode
import models.register.DeclarationWorkingKnowledge
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.declarationWorkingKnowledge

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationWorkingKnowledgeController @Inject()(
                                                       dataCacheConnector: UserAnswersCacheConnector,
                                                       @Register navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: DeclarationWorkingKnowledgeFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       val view: declarationWorkingKnowledge
                                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DeclarationWorkingKnowledgeId) match {
        case None => form
        case Some(value) => form.fill(value.hasWorkingKnowledge)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          dataCacheConnector.save(request.externalId, DeclarationWorkingKnowledgeId,
            DeclarationWorkingKnowledge.declarationWorkingKnowledge(value)).map(cacheMap =>
            Redirect(navigator.nextPage(DeclarationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
        }
      )
  }
}
