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

package services

import com.google.inject.ImplementedBy
import config.FeatureSwitchManagementService
import connectors.{DeRegistrationConnector, SubscriptionConnector, UserAnswersCacheConnector}
import identifiers.UpdateModeId
import identifiers.register.RegistrationInfoId
import identifiers.register.company.BusinessDetailsId
import identifiers.register.company.directors.IsDirectorCompleteId
import identifiers.register.individual.{ExistingCurrentAddressId, IndividualContactAddressId, IndividualDetailsId}
import identifiers.register.partnership.PartnershipDetailsId
import identifiers.register.partnership.partners.IsPartnerCompleteId
import javax.inject.Inject
import models.RegistrationLegalStatus
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.{AuthenticatedRequest, DataRequest}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsResult, JsSuccess, JsValue}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Toggles.{isDeregistrationEnabled, isVariationsEnabled}
import utils.countryOptions.CountryOptions
import utils.{PsaDetailsHelper, UserAnswers, ViewPsaDetailsHelper}
import viewmodels.PsaViewDetailsViewModel
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PsaDetailServiceImpl])
trait PsaDetailsService {
  def retrievePsaDataAndGenerateViewModel(psaId: String)(implicit hc: HeaderCarrier,
                                                         ec: ExecutionContext, request: AuthenticatedRequest[_]): Future[PsaViewDetailsViewModel]
}

class PsaDetailServiceImpl @Inject()(
                                      fs: FeatureSwitchManagementService,
                                      override val messagesApi: MessagesApi,
                                      subscriptionConnector: SubscriptionConnector,
                                      countryOptions: CountryOptions,
                                      deRegistrationConnector: DeRegistrationConnector,
                                      userAnswersCacheConnector: UserAnswersCacheConnector
                                    ) extends PsaDetailsService with I18nSupport {

  override def retrievePsaDataAndGenerateViewModel(psaId: String)(
    implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthenticatedRequest[_]): Future[PsaViewDetailsViewModel] = {
    canStopBeingAPsa(psaId) flatMap { canDeregister =>
      if (fs.get(isVariationsEnabled)) {
        retrievePsaDataFromUserAnswers(psaId, canDeregister)
      } else {
        retrievePsaDataFromModel(psaId)
      }
    }
  }

  def retrievePsaDataFromUserAnswers(psaId: String, canDeregister: Boolean
                                    )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthenticatedRequest[_]): Future[PsaViewDetailsViewModel] = {
    subscriptionConnector.getSubscriptionDetails(psaId) flatMap { response =>
      val answers = UserAnswers(response)
      val legalStatus = answers.get(RegistrationInfoId) map (_.legalStatus)
      val userAnswers = setAdditionalInfoToUserAnswers(answers, legalStatus).flatMap(_.set(UpdateModeId)(true)).asOpt.getOrElse(answers)

      userAnswersCacheConnector.upsert(request.externalId, userAnswers.json).flatMap { _ =>
        val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
        val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated()
        Future.successful(
          legalStatus match {
            case Some(Individual) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).individualSections,
                userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case Some(LimitedCompany) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).companySections,
                userAnswers.get(BusinessDetailsId).map(_.companyName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case Some(Partnership) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).partnershipSections,
                userAnswers.get(PartnershipDetailsId).map(_.companyName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case _ =>
              PsaViewDetailsViewModel(Nil, "", isUserAnswerUpdated, canDeregister)
          })
      }
    }
  }

  private def retrievePsaDataFromModel(psaId: String)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaViewDetailsViewModel] = {
    subscriptionConnector.getSubscriptionModel(psaId).map { response =>
      response.organisationOrPartner match {
        case None =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).individualSections,
            response.individual.map(_.fullName).getOrElse(""),
            false,
            false
          )
        case _ =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).organisationSections,
            response.organisationOrPartner.map(_.name).getOrElse(""),
            false,
            false
          )
      }
    }
  }

  private def setAdditionalInfoToUserAnswers(userAnswers: UserAnswers, legalStatus: Option[RegistrationLegalStatus]): JsResult[UserAnswers] = {
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
    userAnswers.setAllFlagsTrue(seqOfIds, true).flatMap(setAllExistingAddress(_))
  }

  private def setAllExistingAddress(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.get(IndividualContactAddressId).map { address =>
      userAnswers.set(ExistingCurrentAddressId)(address.toTolerantAddress)
    }.getOrElse(JsSuccess(userAnswers))
  }

  private def canStopBeingAPsa(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    if (fs.get(isDeregistrationEnabled)) {
      deRegistrationConnector.canDeRegister(psaId)
    } else {
      Future.successful(false)
    }
  }
}
