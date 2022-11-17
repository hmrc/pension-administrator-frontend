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

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{Retrievals, Variations}
import forms.register.StillUseAdviserFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.VariationStillDeclarationWorkingKnowledgeId
import identifiers.register.adviser.AdviserNameId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.{Navigator, annotations, UserAnswers, Enumerable}
import views.html.register.stillUseAdviser

import scala.concurrent.{Future, ExecutionContext}

class StillUseAdviserController @Inject()(appConfig: FrontendAppConfig,
                                          override val cacheConnector: UserAnswersCacheConnector,
                                          @annotations.Variations navigator: Navigator,
                                          authenticate: AuthAction,
                                          @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: StillUseAdviserFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: stillUseAdviser
                                          )(implicit val executionContext: ExecutionContext
                                          ) extends FrontendBaseController with Enumerable.Implicits  with I18nSupport with Variations with Retrievals {

  private def form(): Form[Boolean] =
    formProvider()

  private def adviserName()(implicit request: DataRequest[AnyContent]) =
    request.userAnswers.get(AdviserNameId).getOrElse("")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      Ok(view(form(),
        mode,
        psaName(),
        displayReturnLink,
        adviserName()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      form().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors,
            mode,
            psaName(),
            displayReturnLink,
            adviserName()))),
        value => {
          cacheConnector.save(request.externalId, VariationStillDeclarationWorkingKnowledgeId, value).map(cacheMap =>
            Redirect(navigator.nextPage(VariationStillDeclarationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
        }
      )
  }
}
