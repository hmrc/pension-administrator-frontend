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
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes.VariationDeclarationController
import identifiers.UpdateContactAddressId
import identifiers.register._
import javax.inject.Inject
import models._
import models.register.DeclarationWorkingKnowledge
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.annotations.Variations
import utils.{Navigator, UserAnswers}
import views.html.register.variationDeclaration

import scala.concurrent.{Future, ExecutionContext}

class VariationDeclarationController @Inject()(val appConfig: FrontendAppConfig,
                                               authenticate: AuthAction,
                                               @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Variations navigator: Navigator,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               pensionsSchemeConnector: PensionsSchemeConnector,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: variationDeclaration
                                              )(implicit val executionContext: ExecutionContext)
                                                extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      val workingKnowledge = request.userAnswers.get(VariationWorkingKnowledgeId).getOrElse(false)
      Future.successful(Ok(view(
        if(displayReturnLink) psaName() else None,
        workingKnowledge,
        VariationDeclarationController.onClickAgree()
      )))
  }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val workingKnowledge = request.userAnswers.get(VariationWorkingKnowledgeId).getOrElse(false)
      dataCacheConnector.save(request.externalId, DeclarationId, value = true).flatMap { json =>

        val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
        val answers = UserAnswers(json).set(ExistingPSAId)(ExistingPSA(
          request.user.isExistingPSA,
          request.user.existingPSAId
        )).asOpt.getOrElse(UserAnswers(json))
          .set(DeclarationWorkingKnowledgeId)(
            DeclarationWorkingKnowledge.declarationWorkingKnowledge(workingKnowledge))
          .asOpt.getOrElse(UserAnswers(json))

        pensionsSchemeConnector.updatePsa(psaId, answers).map(_ =>
          Redirect(navigator.nextPage(DeclarationId, mode, UserAnswers(json)))
        )
      }
  }
}
