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

package controllers.register.partnership

import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.UTRController
import identifiers.register.BusinessUTRId
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import views.html.register.utr

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipUTRController @Inject()(
                                          override val cacheConnector: UserAnswersCacheConnector,
                                          @Partnership override val navigator: Navigator,
                                          authenticate: AuthAction,
                                          override val allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: utr
                                        )(implicit val executionContext: ExecutionContext) extends UTRController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(BusinessUTRId, Message("thePartnership"), Message("utr.partnership.hint"), href)
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      post(BusinessUTRId, Message("thePartnership"), Message("utr.partnership.hint"), href, NormalMode)
  }

  def href: Call = routes.PartnershipUTRController.onSubmit

}
