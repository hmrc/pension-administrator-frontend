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
import models.requests.AuthenticatedRequest
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

    if (fs.get(isVariationsEnabled)) {
      retrievePsaDataFromUserAnswers(psaId)
    } else {
      canStopBeingAPsa(psaId).flatMap{ canDeregister =>
        retrievePsaDataFromModel(psaId, canDeregister)
      }
    }
  }

  def retrievePsaDataFromUserAnswers(psaId: String
                                    )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthenticatedRequest[_]): Future[PsaViewDetailsViewModel] = {
    for {
      response <- subscriptionConnector.getSubscriptionDetails(psaId)
      userAnswers <- getUpdatedUserAnswers(response)
      _ <- userAnswersCacheConnector.upsert(request.externalId, userAnswers.json)
      canDeregister <- canStopBeingAPsa(psaId)
    } yield {
      getPsaDetailsViewModel(userAnswers, canDeregister)
    }
  }

  private def getUpdatedUserAnswers(response: JsValue)(implicit ec: ExecutionContext): Future[UserAnswers] = {
    val answers = UserAnswers(response)
    val legalStatus = answers.get(RegistrationInfoId) map (_.legalStatus)
    Future(setAdditionalInfoToUserAnswers(answers, legalStatus).flatMap(_.set(UpdateModeId)(true)).asOpt.getOrElse(answers))
  }

  private def getPsaDetailsViewModel(userAnswers: UserAnswers, canDeRegister: Boolean): PsaViewDetailsViewModel = {
    val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated()
    val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
    val viewPsaDetailsHelper = new ViewPsaDetailsHelper(userAnswers, countryOptions)

    val (superSections, name) = legalStatus match {
      case Some(Individual) =>
        (viewPsaDetailsHelper.individualSections,
          userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""))

      case Some(LimitedCompany) => (
        viewPsaDetailsHelper.companySections,
        userAnswers.get(BusinessDetailsId).map(_.companyName).getOrElse(""))

      case Some(Partnership) => (
        viewPsaDetailsHelper.partnershipSections,
        userAnswers.get(PartnershipDetailsId).map(_.companyName).getOrElse(""))

      case unknownStatus =>
        throw new IllegalArgumentException(s"Unknown Legal Status : $unknownStatus")
    }

    PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated, canDeRegister)
  }

  private def setAdditionalInfoToUserAnswers(userAnswers: UserAnswers, legalStatus: Option[RegistrationLegalStatus]): JsResult[UserAnswers] = {
    val seqOfCompleteIds = legalStatus match {
      case Some(LimitedCompany) =>
        val directors = userAnswers.allDirectors
        directors.filterNot(_.isDeleted).map(director => IsDirectorCompleteId(directors.indexOf(director))).toList
      case Some(Partnership) =>
        val partners = userAnswers.allPartners
        partners.filterNot(_.isDeleted).map(partner => IsPartnerCompleteId(partners.indexOf(partner))).toList
      case _ => Nil
    }
    userAnswers.setAllFlagsTrue(seqOfCompleteIds).flatMap(setAllExistingAddress)
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

  private def retrievePsaDataFromModel(psaId: String, canDeRegister: Boolean)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaViewDetailsViewModel] = {
    subscriptionConnector.getSubscriptionModel(psaId).map { response =>
      response.organisationOrPartner match {
        case None =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).individualSections,
            response.individual.map(_.fullName).getOrElse(""),
            isUserAnswerUpdated = false,
            canDeRegister
          )
        case _ =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).organisationSections,
            response.organisationOrPartner.map(_.name).getOrElse(""),
            isUserAnswerUpdated = false,
            canDeRegister
          )
      }
    }
  }
}
