/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.SubscriptionConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.PsaDetailsHelper
import utils.countryOptions.CountryOptions
import views.html.psa_details

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                     subscriptionConnector: SubscriptionConnector,
                                     countryOptions: CountryOptions) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authenticate).async {
    implicit request =>
//      request.user.existingPSAId.map { psaId =>
        subscriptionConnector.getSubscriptionDetails("A2100008").map { response =>
              val x = response.organisationOrPartner match {
                case None => (new PsaDetailsHelper(response, countryOptions).individualSections,
                  response.individual.map(_.fullName))
                case _ => (new PsaDetailsHelper(response, countryOptions).organisationSections,
                  response.organisationOrPartner.map(_.name))
              }
          Ok(psa_details(appConfig, x._1, x._2.getOrElse(""), controllers.routes.PsaDetailsController.onPageLoad()))
        }
//      }

  }

}

