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

import com.google.inject.Singleton
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.routes.DeclarationController
import identifiers.register.DeclarationId
import javax.inject.Inject
import models.{Mode, NormalMode, UserType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Navigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      authenticate: AuthAction,
                                      allowAccess: AllowAccessActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      @Register navigator: Navigator,
                                      dataCacheConnector: UserAnswersCacheConnector
                                     )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val cancelUrl = request.user.userType match {
        case UserType.Individual => individual.routes.WhatYouWillNeedController.onPageLoad()
        case UserType.Organisation => company.routes.WhatYouWillNeedController.onPageLoad()
      }
      Future.successful(Ok(declaration(appConfig, cancelUrl, DeclarationController.onClickAgree())))
  }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      dataCacheConnector.save(request.externalId, DeclarationId, value = true).map { cacheMap =>
        Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
      }
  }
}
