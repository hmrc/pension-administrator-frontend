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

package controllers.register

import controllers.actions.AuthAction
import models.register.RegistrationStatus
import models.{CheckMode, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.invlidEmailAddress

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InvalidEmailAddressController @Inject()(
  authenticate: AuthAction,
  val controllerComponents: MessagesControllerComponents,
  val view: invlidEmailAddress
)(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(status: RegistrationStatus): Action[AnyContent] = authenticate {
    implicit request =>
      Ok(view(getJourneyUrl(CheckMode, status)))
  }

  private def getJourneyUrl(mode: Mode, status: RegistrationStatus): Call = {
      status match {
        case RegistrationStatus.LimitedCompany =>
          controllers.register.company.routes.CompanyEmailController.onPageLoad(mode)
        case RegistrationStatus.Partnership =>
          controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(mode)
        case RegistrationStatus.Individual =>
          controllers.register.individual.routes.IndividualEmailController.onPageLoad(mode)
      }
  }

}