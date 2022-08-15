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

package controllers.register.company.contactdetails

import connectors.cache.FeatureToggleConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.BusinessNameId
import models.FeatureToggleName.PsaRegistration
import models.NormalMode
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import viewmodels.Message
import views.html.register.company.contactdetails

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WhatYouWillNeedController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           @AuthWithNoIV authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           whatYouWillNeedView: contactdetails.whatYouWillNeed,
                                           featureToggleConnector: FeatureToggleConnector
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async { implicit request =>
    featureToggleConnector.get(PsaRegistration.asString).map { feature =>
      val returnLinkCompanyName = if (feature.isEnabled) Some(companyName) else None
      Ok(whatYouWillNeedView(companyName, returnLinkCompanyName))
    }
  }

  def onSubmit(): Action[AnyContent] = authenticate { _ =>
    Redirect(controllers.register.company.routes.CompanySameContactAddressController.onPageLoad(NormalMode))
  }

  private def companyName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany"))
}
