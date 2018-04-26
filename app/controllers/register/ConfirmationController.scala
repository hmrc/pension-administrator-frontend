/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import identifiers.register.{ConfirmationId, PsaSubscriptionResponseId}
import models.NormalMode
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import views.html.register.confirmation

import scala.concurrent.Future

class ConfirmationController @Inject()(appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       navigator: Navigator) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      //request.isExistingPSA
   //   PsaSubscriptionResponseId.retrieve.right.map { response =>
        Future.successful(Ok(confirmation(appConfig, "response.psaId")))
    //  }
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    request =>
      Redirect(navigator.nextPage(ConfirmationId, NormalMode)(request.userAnswers))
  }

}
