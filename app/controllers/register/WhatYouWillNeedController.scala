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
import controllers.actions.AuthAction
import javax.inject.Inject
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.RegisterCompany
import views.html.register.whatYouWillNeed

import scala.concurrent.ExecutionContext

class WhatYouWillNeedController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          @RegisterCompany navigator: Navigator,
                                          authenticate: AuthAction
                                         )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {
  def onPageLoad(mode: Mode): Action[AnyContent] = authenticate {
    implicit request =>
      val href = controllers.register.routes.RegisterAsBusinessController.onPageLoad()
      Ok(whatYouWillNeed(appConfig, href))
  }
}