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

package controllers.register.company.workingknowledge

import connectors.cache.FeatureToggleConnector
import controllers.Retrievals
import controllers.actions._
import models.FeatureToggleName.PsaRegistration
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.workingknowledge.whatYouWillNeed

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedController @Inject()(authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: whatYouWillNeed,
                                          featureToggleConnector: FeatureToggleConnector
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val href = controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        Future.successful(Ok(view(href, psaName(), returnLink)))
      }
  }
}
