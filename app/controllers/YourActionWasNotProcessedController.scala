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

package controllers

import controllers.actions.{AuthAction, DataRetrievalAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.yourActionWasNotProcessed

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourActionWasNotProcessedController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     view: yourActionWasNotProcessed,
                                                     psaDetailsService: PsaDetailsService
                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (authenticate andThen getData).async {
      implicit request =>
        request.user.alreadyEnrolledPsaId match {
          case Some(psaId) =>
            psaDetailsService.retrievePsaDataAndGenerateViewModel(psaId).map {
              psaDetails =>
                Ok(view(Some(psaDetails)))
            }
          case _ =>
            Future.successful(Ok(view(None)))
        }
    }
}