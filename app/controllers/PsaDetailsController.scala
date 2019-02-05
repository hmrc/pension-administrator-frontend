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
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.{DeRegistrationConnector, SubscriptionConnector}
import controllers.actions.AuthAction
import identifiers.register.RegistrationInfoId
import identifiers.register.company.BusinessDetailsId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Toggles.{isDeregistrationEnabled, isVariationsEnabled}
import utils.countryOptions.CountryOptions
import utils.{PsaDetailsHelper, UserAnswers, ViewPsaDetailsHelper}
import viewmodels.SuperSection
import views.html.psa_details

import scala.concurrent.{ExecutionContext, Future}

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                     subscriptionConnector: SubscriptionConnector,
                                     deRegistrationConnector: DeRegistrationConnector,
                                     countryOptions: CountryOptions,
                                     fs: FeatureSwitchManagementService
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = authenticate.async {
    implicit request =>
      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
      val retrieval = if(fs.get(isVariationsEnabled)) retrievePsaDataFromUserAnswers(psaId) else retrievePsaDataFromModel(psaId)
      canStopBeingAPsa(psaId) flatMap { canDeregister =>
        retrieval map { tuple => Ok(psa_details(appConfig, tuple._1, tuple._2, canDeregister, false)) }
      }
  }

  private def retrievePsaDataFromModel(psaId: String)(implicit hc: HeaderCarrier): Future[(Seq[SuperSection], String, Boolean)] = {
      subscriptionConnector.getSubscriptionModel(psaId).map { response =>
      response.organisationOrPartner match {
        case None =>
          (new PsaDetailsHelper(response, countryOptions).individualSections, response.individual.map(_.fullName).getOrElse(""), false)
        case _ =>
          (new PsaDetailsHelper(response, countryOptions).organisationSections, response.organisationOrPartner.map(_.name).getOrElse(""), false)
      }
    }
  }

  private def retrievePsaDataFromUserAnswers(psaId: String)(implicit hc: HeaderCarrier): Future[(Seq[SuperSection], String, Boolean)] = {
    subscriptionConnector.getSubscriptionDetails(psaId) flatMap { response =>
      val userAnswers = UserAnswers(response)
      val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
      val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated()
      Future.successful(
        legalStatus match {
          case Some(Individual) =>
            (new ViewPsaDetailsHelper(userAnswers, countryOptions).individualSections,
              userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""), isUserAnswerUpdated)
          case Some(LimitedCompany) =>
            (new ViewPsaDetailsHelper(userAnswers, countryOptions).companySections,
              userAnswers.get(BusinessDetailsId).map(_.companyName).getOrElse(""), isUserAnswerUpdated)
          case Some(Partnership) =>
            (new ViewPsaDetailsHelper(userAnswers, countryOptions).partnershipSections,
              userAnswers.get(PartnershipDetailsId).map(_.companyName).getOrElse(""), isUserAnswerUpdated)
          case _ => (Nil, "", isUserAnswerUpdated)
        })
    }
  }

  private def canStopBeingAPsa(psaId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    if (fs.get(isDeregistrationEnabled)) {
      deRegistrationConnector.canDeRegister(psaId)
    } else {
      Future.successful(false)
    }
  }
}
