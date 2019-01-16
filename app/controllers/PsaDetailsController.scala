/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{SubscriptionConnector, UserAnswersCacheConnector}
import controllers.actions.AuthAction
import identifiers.register.PsaNameId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.PsaDetailsHelper
import utils.countryOptions.CountryOptions
import views.html.psa_details

import scala.concurrent.ExecutionContext

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                     subscriptionConnector: SubscriptionConnector,
                                     userAnswersCacheConnector: UserAnswersCacheConnector,
                                     countryOptions: CountryOptions
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = authenticate.async {
    implicit request =>

      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))

      subscriptionConnector.getSubscriptionDetails(psaId).flatMap { response =>

        val psaDetails =  new PsaDetailsHelper(response, countryOptions)

        val (psaName, superSections) = response.organisationOrPartner match {
          case None => (response.individual.map(_.fullName).getOrElse(""), psaDetails.individualSections)
          case _ => (response.organisationOrPartner.map(_.name).getOrElse(""), psaDetails.organisationSections)
        }

        userAnswersCacheConnector.save(request.externalId, PsaNameId, psaName).map { _ =>
          Ok(psa_details(appConfig, superSections, psaName))
        }
      }
  }
}
