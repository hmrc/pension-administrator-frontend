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
import forms.register.VariationDeclarationWorkingKnowledgeFormProvider
import identifiers.register.adviser.AdviserNameId
import identifiers.vary.DeclarationWorkingKnowledgeId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers, annotations}
import views.html.register.variationDeclarationWorkingKnowledge

import scala.concurrent.Future

class VariationDeclarationWorkingKnowledgeController @Inject()(
                                                       appConfig: FrontendAppConfig,
                                                       override val messagesApi: MessagesApi,
                                                       override val cacheConnector: UserAnswersCacheConnector,
                                                       @annotations.Variations navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       allowAccess: AllowAccessActionProvider,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: VariationDeclarationWorkingKnowledgeFormProvider
                                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits with Variations with Retrievals {

  private val form = formProvider()

  private def adviserName()(implicit request: DataRequest[AnyContent]) =
    request.userAnswers.get(AdviserNameId).getOrElse("")

  def onPageLoad(mode: Mode) = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>

      Ok(variationDeclarationWorkingKnowledge(appConfig, form, mode, psaName(), adviserName()))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(variationDeclarationWorkingKnowledge(appConfig, formWithErrors, mode, psaName(), adviserName()))),
        value => {
          cacheConnector.save(request.externalId, DeclarationWorkingKnowledgeId, value).map(cacheMap =>
            Redirect(navigator.nextPage(DeclarationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
        }
      )
  }
}
