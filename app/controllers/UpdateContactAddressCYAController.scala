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

package controllers

import com.google.inject.Inject
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRetrievalAction}
import identifiers.register.UpdateContactAddressCYAId
import models._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.{Navigator, UserAnswers}
import views.html.updateContactAddressCYA

import scala.concurrent.{ExecutionContext, Future}

class UpdateContactAddressCYAController @Inject()(
                                                   @utils.annotations.Variations navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                   getData: DataRetrievalAction,
                                                   psaDetailsService: PsaDetailsService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: updateContactAddressCYA
                                                 )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode = UpdateMode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async {
    implicit request =>
      request.user.alreadyEnrolledPsaId.map { psaId =>
        psaDetailsService.retrievePsaDataAndGenerateContactDetailsOnlyViewModel(psaId, mode).map { psaDetails =>
          val nextPage = navigator.nextPage(UpdateContactAddressCYAId, mode, request.userAnswers.getOrElse(UserAnswers()))

          Ok(view(psaDetails, nextPage))
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
      )
  }
}
