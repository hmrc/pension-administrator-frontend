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

package controllers.register.individual

import audit.{AuditService, PSAStartEvent}
import config.FrontendAppConfig
import controllers.actions._
import identifiers.register.individual.WhatYouWillNeedId
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Individual
import views.html.register.individual.whatYouWillNeed

import scala.concurrent.ExecutionContext

class WhatYouWillNeedController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          @Individual val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          auditService: AuditService
                                         )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      Ok(whatYouWillNeed(appConfig))
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      PSAStartEvent.sendEvent(auditService)
      Redirect(navigator.nextPage(WhatYouWillNeedId, NormalMode, request.userAnswers))
  }

}
