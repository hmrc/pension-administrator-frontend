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

import controllers.Execution.trampoline
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRetrievalAction}
import models.Mode
import models.admin.ukResidencyToggle
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import views.html.register.individual.{individualNonUKAdministrator, nonUKAdministrator}

import javax.inject.Inject

class NonUKAdministratorController @Inject()(
                                              @AuthWithNoIV authenticate: AuthAction,
                                              allowAccess: AllowAccessActionProvider,
                                              getData: DataRetrievalAction,
                                              featureFlagService: FeatureFlagService,
                                              val controllerComponents: MessagesControllerComponents,
                                              val view: nonUKAdministrator,
                                              val nonUkResidencyView: individualNonUKAdministrator
                                            ) extends FrontendBaseController with I18nSupport with Retrievals {
  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).map { ukResidency =>
        if (ukResidency.isEnabled) Ok(nonUkResidencyView()) else Ok(view())
      }
  }
}
