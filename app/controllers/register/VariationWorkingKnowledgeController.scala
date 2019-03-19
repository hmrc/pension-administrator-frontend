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

package controllers.register

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.{Retrievals, Variations}
import forms.register.VariationWorkingKnowledgeFormProvider
import identifiers.register.adviser.IsNewAdviserId
import identifiers.register.{DeclarationChangedId, PAInDeclarationJourneyId, VariationWorkingKnowledgeId}
import javax.inject.Inject
import models.{CheckUpdateMode, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers, annotations}
import views.html.register.variationWorkingKnowledge

import scala.concurrent.Future

class VariationWorkingKnowledgeController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val cacheConnector: UserAnswersCacheConnector,
                                                     @annotations.Variations navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: VariationWorkingKnowledgeFormProvider
                                                   ) extends FrontendController with I18nSupport with Enumerable.Implicits with Variations with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(VariationWorkingKnowledgeId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(variationWorkingKnowledge(appConfig, preparedForm, psaName(), mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(variationWorkingKnowledge(appConfig, formWithErrors, psaName(), mode))),
        value => {
          val hasAnswerChanged = request.userAnswers.get(VariationWorkingKnowledgeId) match {
            case None => true
            case Some(existing) => existing != value
          }

          val resultOfSaveDeclarationFlag = mode match {
            case CheckUpdateMode =>
              cacheConnector.save (request.externalId, PAInDeclarationJourneyId, true)
            case _ =>
              Future.successful(())
          }

          resultOfSaveDeclarationFlag.flatMap(_ =>
            cacheConnector.save(request.externalId, IsNewAdviserId, !value).flatMap(_ =>
              if (hasAnswerChanged) {
                cacheConnector.save(request.externalId, VariationWorkingKnowledgeId, value).flatMap(cacheMap =>
                  saveChangeFlag(mode, VariationWorkingKnowledgeId).map(_ =>
                    Redirect(navigator.nextPage(VariationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
                )
              } else {
                cacheConnector.save(request.externalId, VariationWorkingKnowledgeId, value).map(cacheMap =>
                  Redirect(navigator.nextPage(VariationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
              }
            )
          )
        }
      )
  }
}
