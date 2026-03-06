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

package controllers.register.individual

import controllers.actions.*
import identifiers.register.individual.WhatYouWillNeedId
import models.admin.ukResidencyToggle
import models.{Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Navigator
import utils.annotations.{AuthWithNoIV, Individual}
import utils.navigators.IndividualNavigatorV2
import views.html.register.individual.whatYouWillNeed

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedController @Inject()(@Individual val navigator: Navigator,
                                          val navigatorV2: IndividualNavigatorV2,
                                          @AuthWithNoIV authenticate: AuthAction,
                                          allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          featureFlagService: FeatureFlagService,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: whatYouWillNeed
                                         )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).flatMap { ukResidency =>
        if (ukResidency.isEnabled) {
          Future.successful(Ok(view(ukResidencyToggle = true)))
        } else {
          Future.successful(Ok(view(ukResidencyToggle = false)))
        }
      }
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).flatMap { ukResidency =>
        val selectedNavigator = if (ukResidency.isEnabled) navigatorV2 else navigator
        Future.successful(Redirect(selectedNavigator.nextPage(WhatYouWillNeedId, NormalMode, request.userAnswers)))
      }
  }
}
