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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.UTRController
import identifiers.register.{BusinessTypeId, BusinessUTRId}
import javax.inject.Inject
import models.NormalMode
import models.register.BusinessType
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import views.html.register.utr

import scala.concurrent.ExecutionContext

class PartnershipUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val cacheConnector: UserAnswersCacheConnector,
                                         @Partnership override val navigator: Navigator,
                                         authenticate: AuthAction,
                                         override val allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: utr
                                        )(implicit messages: Messages, val executionContext: ExecutionContext) extends UTRController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessTypeId.retrieve.right.map { businessType =>
        get(BusinessUTRId, toString(businessType), href)
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      BusinessTypeId.retrieve.right.map { businessType =>
        post(BusinessUTRId, toString(businessType), href, NormalMode)
      }
  }

  def href: Call = routes.PartnershipUTRController.onSubmit()

  def toString(businessType: BusinessType): String = Message(s"businessType.${businessType.toString}").resolve.toLowerCase()

}
