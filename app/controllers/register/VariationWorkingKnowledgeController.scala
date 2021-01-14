/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{Retrievals, Variations}
import forms.register.VariationWorkingKnowledgeFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser.IsNewAdviserId
import identifiers.register.{PAInDeclarationJourneyId, VariationWorkingKnowledgeId}
import javax.inject.Inject
import models.{Mode, CheckUpdateMode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.{Navigator, annotations, UserAnswers, Enumerable}
import views.html.register.variationWorkingKnowledge

import scala.concurrent.{Future, ExecutionContext}

class VariationWorkingKnowledgeController @Inject()(appConfig: FrontendAppConfig,
                                                    override val cacheConnector: UserAnswersCacheConnector,
                                                    @annotations.Variations navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: VariationWorkingKnowledgeFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: variationWorkingKnowledge
                                                   )(implicit val executionContext: ExecutionContext)
                                                     extends FrontendBaseController with I18nSupport
                                                     with Enumerable.Implicits with Variations with Retrievals {

  private def form(): Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      val preparedForm = request.userAnswers.get(VariationWorkingKnowledgeId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(
        preparedForm,
        if(displayReturnLink) psaName() else None,
        mode
      ))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(
            formWithErrors,
            if(displayReturnLink) psaName() else None,
            mode
          ))),
        value => {
          val resultOfSaveDeclarationFlag = mode match {
            case CheckUpdateMode =>
              cacheConnector.save(request.externalId, PAInDeclarationJourneyId, true)
            case _ =>
              Future.successful(())
          }

          resultOfSaveDeclarationFlag.flatMap(_ =>
            cacheConnector.save(request.externalId, IsNewAdviserId, !value).flatMap(_ =>
              cacheConnector.save(request.externalId, VariationWorkingKnowledgeId, value).flatMap(cacheMap =>
                saveChangeFlag(mode, VariationWorkingKnowledgeId).map(_ =>
                  Redirect(navigator.nextPage(VariationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
              )
            )
          )
        }
      )
  }
}
