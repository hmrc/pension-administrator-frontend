/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{DOBController, Retrievals}
import identifiers.register.BusinessNameId
import identifiers.register.partnership.partners.{PartnerDOBId, PartnerNameId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.dob

import scala.concurrent.{ExecutionContext, Future}

class PartnerDOBController @Inject()(val appConfig: FrontendAppConfig,
                                     val cacheConnector: UserAnswersCacheConnector,
                                     @PartnershipPartner val navigator: Navigator,
                                     override val allowAccess: AllowAccessActionProvider,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     val controllerComponents: MessagesControllerComponents,
                                     val view: dob
                                    )(implicit val executionContext: ExecutionContext) extends DOBController with Retrievals {

  private[partners] def viewModel(mode: Mode,
                                  index: Index,
                                  psaName: String,
                                  partnerName: String) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerDOBController.onSubmit(mode, index),
      title = "partnerDob.title",
      heading = Message("dob.heading", partnerName),
      None,
      None,
      mode,
      psaName
    )

  private[partners] def id(index: Index): PartnerDOBId =
    PartnerDOBId(index)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      (BusinessNameId and PartnerNameId(index)).retrieve.right.map {
        case psaName ~ partnerName =>
          Future(get(id(index), viewModel(mode, index, psaName, partnerName.fullName)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      (BusinessNameId and PartnerNameId(index)).retrieve.right.map {
        case psaName ~ partnerName =>
          post(id(index), viewModel(mode, index, psaName, partnerName.fullName))
      }
  }

}
