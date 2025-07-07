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

package controllers

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRetrievalAction}
import identifiers.UpdateContactAddressId
import identifiers.register.RegistrationInfoId
import identifiers.register.company.CompanyContactAddressId
import identifiers.register.individual.IndividualContactAddressId
import identifiers.register.partnership.PartnershipContactAddressId
import models.RegistrationLegalStatus.*
import models.{Address, UpdateMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import views.html.updateContactAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateContactAddressController @Inject()(val appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            psaDetailsService: PsaDetailsService,
                                            countryOptions: CountryOptions,
                                            userAnswersCacheConnector: UserAnswersCacheConnector,
                                            @utils.annotations.Variations val navigator: Navigator,
                                            val view: updateContactAddress
                                           )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData).async { implicit request =>
    psaDetailsService.getUserAnswers.flatMap { userAnswersUpdated =>
      getAddress(userAnswersUpdated) match {
        case Some(address) =>
          val ua = userAnswersUpdated.setOrException(UpdateContactAddressId)(true)
          userAnswersCacheConnector
            .upsert(ua.json)
            .map(_ =>
              Ok(view(address.lines(countryOptions), navigator.nextPage(UpdateContactAddressId, UpdateMode, ua).url))
            )
        case None => Future.successful(sessionExpired)
      }
    }
  }

  private def getAddress(ua: UserAnswers): Option[Address] = {
    ua.get(RegistrationInfoId).map {
      regInfo =>
        regInfo.legalStatus match {
          case LimitedCompany => ua.getOrException(CompanyContactAddressId)
          case Individual => ua.getOrException(IndividualContactAddressId)
          case Partnership => ua.getOrException(PartnershipContactAddressId)
        }
    }
  }

  private def sessionExpired:Result = Redirect(controllers.routes.SessionExpiredController.onPageLoad)
}
