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
import connectors.{DeRegistrationConnector, SubscriptionConnector, UserAnswersCacheConnector}
import controllers.actions.AuthAction
import identifiers.UpdateModeId
import identifiers.register.RegistrationInfoId
import identifiers.register.company.BusinessDetailsId
import identifiers.register.company.directors.IsDirectorCompleteId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import identifiers.register.partnership.partners.IsPartnerCompleteId
import models.RegistrationLegalStatus
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.AuthenticatedRequest
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
                                     dataCacheConnector: UserAnswersCacheConnector,
                                     countryOptions: CountryOptions,
                                     fs: FeatureSwitchManagementService
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = authenticate.async {
    implicit request =>
      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
      val retrieval = if (fs.get(isVariationsEnabled)) retrievePsaDataFromUserAnswers(psaId) else retrievePsaDataFromModel(psaId)
      canStopBeingAPsa(psaId) flatMap { canDeregister =>
        retrieval map { tuple => Ok(psa_details(appConfig, tuple._1, tuple._2, canDeregister)) }
      }
  }

  private def retrievePsaDataFromModel(psaId: String)(implicit hc: HeaderCarrier): Future[(Seq[SuperSection], String)] = {
    subscriptionConnector.getSubscriptionModel(psaId).map { response =>
      response.organisationOrPartner match {
        case None =>
          (new PsaDetailsHelper(response, countryOptions).individualSections, response.individual.map(_.fullName).getOrElse(""))
        case _ =>
          (new PsaDetailsHelper(response, countryOptions).organisationSections, response.organisationOrPartner.map(_.name).getOrElse(""))
      }
    }
  }

  private def retrievePsaDataFromUserAnswers(psaId: String)(
    implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]): Future[(Seq[SuperSection], String)] = {
    subscriptionConnector.getSubscriptionDetails(psaId) flatMap { response =>
      val answers = UserAnswers(response)
      val legalStatus = answers.get(RegistrationInfoId) map (_.legalStatus)
      val userAnswers = setAllCompleteFlags(answers, legalStatus).flatMap(_.set(UpdateModeId)(true)).asOpt.getOrElse(answers)
      dataCacheConnector.upsert(request.externalId, userAnswers.json).flatMap { _ =>
        Future.successful(
          legalStatus match {
            case Some(Individual) =>
              (new ViewPsaDetailsHelper(
                userAnswers, countryOptions).individualSections, userAnswers.get(IndividualDetailsId) map (_.fullName) getOrElse "")
            case Some(LimitedCompany) =>
              (new ViewPsaDetailsHelper(
                userAnswers, countryOptions).companySections, userAnswers.get(BusinessDetailsId) map (_.companyName) getOrElse "")
            case Some(Partnership) =>
              (new ViewPsaDetailsHelper(
                userAnswers, countryOptions).partnershipSections,
                userAnswers.get(PartnershipDetailsId) map (_.companyName) getOrElse "")
            case _ => (Nil, "")
          })
      }
    }
  }

  private def setAllCompleteFlags(userAnswers: UserAnswers, legalStatus: Option[RegistrationLegalStatus]) = {
    val seqOfIds = legalStatus match {
      case Some(LimitedCompany) =>
        val directors = userAnswers.allDirectors
        directors.filterNot(_.isDeleted).map { director =>
          IsDirectorCompleteId(directors.indexOf(director))
        }.toList
      case Some(Partnership) =>
        val partners = userAnswers.allPartners
        partners.filterNot(_.isDeleted).map { partner =>
          IsPartnerCompleteId(partners.indexOf(partner))
        }.toList
      case _ => Nil
    }
    userAnswers.setAllFlagsTrue(seqOfIds)
  }

  private def canStopBeingAPsa(psaId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    if (fs.get(isDeregistrationEnabled)) {
      deRegistrationConnector.canDeRegister(psaId)
    } else {
      Future.successful(false)
    }
  }
}
