/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import controllers.actions.AuthAction
import controllers.actions.DataRequiredAction
import controllers.actions.DataRetrievalAction
import identifiers.register.RegistrationInfoId
import identifiers.register.company.CompanyContactAddressId
import identifiers.register.individual.IndividualContactAddressId
import identifiers.register.partnership.PartnershipContactAddressId
import javax.inject.Inject
import models.Address
import models.CheckMode
import models.RegistrationLegalStatus._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.Result
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import utils.countryOptions.CountryOptions
import views.html.updateContactAddress

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UpdateContactAddressController @Inject()(val appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            psaDetailsService: PsaDetailsService,
                                            countryOptions: CountryOptions,
                                            val view: updateContactAddress
                                           )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async { implicit request =>
    request.user.alreadyEnrolledPsaId match {
      case Some(psaId) =>
        psaDetailsService.getUserAnswers(psaId, request.externalId).map { ua =>
          retrieveRequiredValues(ua) match {
            case Some(Tuple2(continueUrl, address)) =>
              Ok(view(address.lines(countryOptions), continueUrl))
            case None => sessionExpired
          }
        }
      case None => Future.successful(sessionExpired)
    }
  }

  private def retrieveRequiredValues(ua: UserAnswers): Option[(String, Address)] = {
    ua.get(RegistrationInfoId).flatMap {
      regInfo =>
        regInfo.legalStatus match {
          case LimitedCompany => Some(
            Tuple2(
              controllers.register.company.routes.CompanyContactAddressController.onPageLoad(CheckMode).url,
              ua.getOrException(CompanyContactAddressId)
            )
          )
          case Individual => Some(
            Tuple2(
              controllers.register.individual.routes.IndividualContactAddressController.onPageLoad(CheckMode).url,
              ua.getOrException(IndividualContactAddressId)
            )
          )
          case Partnership => Some(
            Tuple2(
              controllers.register.partnership.routes.PartnershipContactAddressController.onPageLoad(CheckMode).url,
              ua.getOrException(PartnershipContactAddressId)
            )
          )
          case _ => None
        }
    }
  }

  private def sessionExpired:Result = Redirect(controllers.routes.SessionExpiredController.onPageLoad())
}
