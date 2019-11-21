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
import identifiers.register._
import javax.inject.Inject
import models.{Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Register
import utils.{Navigator, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  dataCacheConnector: UserAnswersCacheConnector,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: declarationFitAndProper
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Ok(view()))
  }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, value = true).map { cacheMap =>
        Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode, UserAnswers(cacheMap)))
      }
  }


}
