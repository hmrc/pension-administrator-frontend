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

package controllers.register.company.directors

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.register.company.directors.whatYouWillNeed

import scala.concurrent.Future

class WhatYouWillNeedController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          val allowAccess: AllowAccessActionProvider,
                                          authenticate: AuthAction
                                         ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode)).async {
    implicit request =>
      val href = controllers.routes.IndexController.onPageLoad()
      Future.successful(Ok(whatYouWillNeed(appConfig, href)))
  }
}