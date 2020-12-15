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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.SubscriptionConnector
import connectors.cache.UserAnswersCacheConnector
import identifiers.register._
import identifiers.register.company.directors.{DirectorAddressId, ExistingCurrentAddressId => DirectorsExistingCurrentAddressId}
import identifiers.register.company.{ExistingCurrentAddressId => CompanyExistingCurrentAddressId, _}
import identifiers.register.individual._
import identifiers.register.partnership.partners.{PartnerAddressId, ExistingCurrentAddressId => PartnersExistingCurrentAddressId}
import identifiers.register.partnership.{ExistingCurrentAddressId => PartnershipExistingCurrentAddressId, _}
import identifiers.{IndexId, TypedIdentifier, UpdateModeId}
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models._
import models.requests.OptionalDataRequest
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.{UserAnswers, ViewPsaDetailsHelper}
import viewmodels.{PsaViewDetailsViewModel, SuperSection}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PsaDetailServiceImpl])
trait PsaDetailsService {
  def retrievePsaDataAndGenerateViewModel(psaId: String, mode: Mode)
                                         (implicit hc: HeaderCarrier,
                                          executionContext: ExecutionContext,
                                          request: OptionalDataRequest[_],
                                          messages: Messages): Future[PsaViewDetailsViewModel]
  def getUserAnswers(psaId: String, externalId: String)(implicit hc: HeaderCarrier,
    executionContext: ExecutionContext): Future[UserAnswers]
}

class PsaDetailServiceImpl @Inject()(subscriptionConnector: SubscriptionConnector,
                                     countryOptions: CountryOptions,
                                     userAnswersCacheConnector: UserAnswersCacheConnector,
                                     dataCompletion: DataCompletion
                                    ) extends PsaDetailsService {

  override def retrievePsaDataAndGenerateViewModel(psaId: String, mode: Mode)
                                                  (implicit hc: HeaderCarrier,
                                                   executionContext: ExecutionContext,
                                                   request: OptionalDataRequest[_],
                                                   messages: Messages): Future[PsaViewDetailsViewModel] = {

    retrievePsaDataFromUserAnswers(psaId, mode)

  }

  def retrievePsaDataFromUserAnswers(psaId: String, mode: Mode
                                    )(implicit hc: HeaderCarrier, executionContext: ExecutionContext,
                                      request: OptionalDataRequest[_], messages: Messages): Future[PsaViewDetailsViewModel] = {
    for {
      userAnswers <- getUserAnswers(psaId, request.externalId)
      _ <- userAnswersCacheConnector.upsert(request.externalId, userAnswers.json)
    } yield {
      getPsaDetailsViewModel(userAnswers)
    }
  }

  def getUserAnswers(psaId: String, externalId: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[UserAnswers] =
    userAnswersCacheConnector.fetch(externalId).flatMap {
      case None => subscriptionConnector.getSubscriptionDetails(psaId).flatMap{getUpdatedUserAnswers}
      case Some(data) =>
        (UserAnswers(data).get(IndexId), UserAnswers(data).get(RegistrationInfoId)) match {
          case (Some(_), None) =>
            userAnswersCacheConnector.removeAll(externalId).flatMap { _ =>
              subscriptionConnector.getSubscriptionDetails(psaId).flatMap {getUpdatedUserAnswers}
            }
          case _ =>
            Future(UserAnswers(data))
        }
    }

  private val changeFlagIds = List(
    IndividualAddressChangedId,
    IndividualPreviousAddressChangedId,
    IndividualContactDetailsChangedId,
    CompanyContactAddressChangedId,
    CompanyPreviousAddressChangedId,
    CompanyContactDetailsChangedId,
    PartnershipContactAddressChangedId,
    PartnershipPreviousAddressChangedId,
    PartnershipContactDetailsChangedId,
    DeclarationChangedId,
    MoreThanTenDirectorsOrPartnersChangedId,
    DirectorsOrPartnersChangedId
  )

  private def getUpdatedUserAnswers(response: JsValue)(implicit executionContext: ExecutionContext): Future[UserAnswers] = {
    val answers = UserAnswers(response)
    val legalStatus = answers.get(RegistrationInfoId) map (_.legalStatus)
    Future.successful(
      setAddressIdsToUserAnswers(answers, legalStatus).flatMap(_.set(UpdateModeId)(true))
        .flatMap(_.setAllFlagsToValue(changeFlagIds, value = false))
        .asOpt.getOrElse(answers)
    )
  }

  private def getPsaDetailsViewModel(userAnswers: UserAnswers)(implicit messages: Messages): PsaViewDetailsViewModel = {
    val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated
    val incompleteMessage = dataCompletion.psaUpdateDetailsInCompleteAlert(userAnswers)
    val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
    val viewPsaDetailsHelper = new ViewPsaDetailsHelper(userAnswers, countryOptions)

    val (superSections, name): (Seq[SuperSection], String) = legalStatus match {
      case Some(Individual) =>
        (viewPsaDetailsHelper.individualSections,
          userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""))

      case Some(LimitedCompany) => (
        viewPsaDetailsHelper.companySections,
        userAnswers.get(BusinessNameId).getOrElse(""))

      case Some(Partnership) => (
        viewPsaDetailsHelper.partnershipSections,
        userAnswers.get(BusinessNameId).getOrElse(""))

      case unknownStatus =>
        throw new IllegalArgumentException(s"Unknown Legal Status : $unknownStatus")
    }

    PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated, incompleteMessage)
  }

  private def setAddressIdsToUserAnswers(userAnswers: UserAnswers,
                                         legalStatus: Option[RegistrationLegalStatus]): JsResult[UserAnswers] = {

    val mapOfAddressIds: Map[TypedIdentifier[Address], TypedIdentifier[TolerantAddress]] = legalStatus match {
      case Some(Individual) =>
        Map(IndividualContactAddressId -> ExistingCurrentAddressId)

      case Some(LimitedCompany) =>
        val allDirectors = userAnswers.allDirectorsAfterDelete(UpdateMode)
        val allDirectorsAddressIdMap = allDirectors.map { director =>
          val index = allDirectors.indexOf(director)
          (DirectorAddressId(index), DirectorsExistingCurrentAddressId(index))
        }.toMap

        Map(CompanyContactAddressId -> CompanyExistingCurrentAddressId) ++ allDirectorsAddressIdMap

      case Some(Partnership) =>
        val allPartners = userAnswers.allPartnersAfterDelete(UpdateMode)
        val allPartnersAddressIds = allPartners.map { partner =>
          val index = allPartners.indexOf(partner)
          (PartnerAddressId(index), PartnersExistingCurrentAddressId(index))
        }.toMap

        Map(PartnershipContactAddressId -> PartnershipExistingCurrentAddressId) ++ allPartnersAddressIds

      case _ =>
        Map.empty
    }
    userAnswers.setAllExistingAddress(mapOfAddressIds)
  }
}
