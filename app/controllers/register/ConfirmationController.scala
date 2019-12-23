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
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{BusinessNameId, PsaNameId, PsaSubscriptionResponseId}
import javax.inject.Inject
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import views.html.register.confirmation

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationController @Inject()(appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       authenticate: AuthAction,
                                       allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: confirmation
                                      )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private def getPSAName(ua:UserAnswers) ={
    (ua.get(BusinessNameId), ua.get(IndividualDetailsId)) match {
      case (Some(name), _) => name
      case (_, Some(p)) => p.fullName
      case _ => throw new RuntimeException("No name found error")
    }
  }

  def onPageLoad(mode:Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PsaSubscriptionResponseId.retrieve.right.map {response  =>
        dataCacheConnector.removeAll(request.externalId)
        Future.successful(Ok(view(response.psaId, getPSAName(request.userAnswers))))
      }
  }

  def onSubmit(mode:Mode): Action[AnyContent] = authenticate {
    _ => Redirect(controllers.routes.LogoutController.onPageLoad())
  }

}
