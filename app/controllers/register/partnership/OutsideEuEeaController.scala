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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipRegisteredAddressId
import javax.inject.Inject
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.register.outsideEuEea

import scala.concurrent.Future

class OutsideEuEeaController @Inject()(appConfig: FrontendAppConfig,
                                       authenticate: AuthAction,
                                       allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       countryOptions: CountryOptions,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: outsideEuEea
                                      ) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      (BusinessNameId and PartnershipRegisteredAddressId).retrieve.map {
        case name ~ address =>
          Future.successful(Ok(view(name, countryOptions.getCountryNameFromCode(address.toAddress.get), "partnerships")))
        }.left.map(_ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad)))

  }

}
