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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.partnership.partners.PartnerNameId
import javax.inject.Inject
import models.{Index, Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

import scala.concurrent.Future

class AlreadyDeletedController @Inject()(appConfig: FrontendAppConfig,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: alreadyDeleted
                                        ) extends FrontendBaseController with Retrievals with Enumerable.Implicits with I18nSupport {

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      def viewmodel(name: String) = AlreadyDeletedViewModel(
        Message("alreadyDeleted.partner.title"),
        name,
        controllers.register.partnership.routes.AddPartnerController.onPageLoad(NormalMode)
      )

      PartnerNameId(index).retrieve.right.map { partnerDetails =>
        Future.successful(Ok(view(viewmodel(partnerDetails.fullName))))
      }

  }

}
