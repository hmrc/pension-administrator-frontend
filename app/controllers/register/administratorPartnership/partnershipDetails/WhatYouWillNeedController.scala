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

package controllers.register.administratorPartnership.partnershipDetails

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.BusinessNameId
import identifiers.register.partnership.WhatYouWillNeedIdV2
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Navigator
import utils.annotations.{AuthWithNoIV, PartnershipV2}
import views.html.register.administratorPartnership.partnershipDetails.whatYouWillNeed

import javax.inject.Inject

class WhatYouWillNeedController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           @AuthWithNoIV authenticate: AuthAction,
                                           @PartnershipV2 navigator: Navigator,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           whatYouWillNeedView: whatYouWillNeed
                                         ) extends FrontendBaseController with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) { implicit request =>
    val partnershipName = request.userAnswers.getOrException(BusinessNameId)
    Ok(whatYouWillNeedView(partnershipName))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(WhatYouWillNeedIdV2, NormalMode, request.userAnswers))
  }
}
