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
import controllers.Retrievals
import controllers.actions._
import identifiers.register.PsaSubscriptionResponseId
import models.Mode
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.confirmation

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        dataCacheConnector: UserAnswersCacheConnector,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: confirmation
                                      )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger = Logger(classOf[ConfirmationController])

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PsaSubscriptionResponseId.retrieve.right.map { response =>
        dataCacheConnector.removeAll(request.externalId).flatMap { _ =>
          (psaName, psaEmail) match {
            case (Some(name), Some(email)) =>
              Future.successful(Ok(view(response.psaId, name, email)))
            case (noPsaName, noEmail) =>
              logger.warn(s"No Psa Name $noPsaName Or Email $noEmail Found")
              Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
          }
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = authenticate {
    _ => Redirect(controllers.routes.LogoutController.onPageLoad())
  }

}
