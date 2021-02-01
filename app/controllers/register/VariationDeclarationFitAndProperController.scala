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
import controllers.Retrievals
import controllers.actions._
import forms.register.VariationDeclarationFitAndProperFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register._
import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{NoRLSCheck, Variations}
import utils.{Navigator, UserAnswers}
import views.html.register.variationDeclarationFitAndProper

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                           authenticate: AuthAction,
                                                           @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           @Variations navigator: Navigator,
                                                           formProvider: VariationDeclarationFitAndProperFormProvider,
                                                           dataCacheConnector: UserAnswersCacheConnector,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           val view: variationDeclarationFitAndProper
                                                          )(implicit val executionContext: ExecutionContext)
                                                            extends FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      val preparedForm = request.userAnswers.get(DeclarationFitAndProperId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(view(
        preparedForm,
        psaName(),
        displayReturnLink
      )))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty
      form.bindFromRequest().fold(
        errors => Future.successful(BadRequest(view(
          errors,
          psaName(),
          displayReturnLink
        ))),
        success => {
          dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).map { json =>
            Redirect(navigator.nextPage(DeclarationFitAndProperId, mode, UserAnswers(json)))
          }
        }
      )
  }
}
