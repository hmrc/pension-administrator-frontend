/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import connectors.SubscriptionConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import identifiers.register.company.directors.{DirectorAddressId, ExistingCurrentAddressId as DirectorsExistingCurrentAddressId}
import identifiers.register.company.{CompanyContactAddressChangedId, CompanyContactAddressId, CompanyContactDetailsChangedId, CompanyPreviousAddressChangedId, ExistingCurrentAddressId as CompanyExistingCurrentAddressId}
import identifiers.register.individual.*
import identifiers.register.partnership.partners.{PartnerAddressId, ExistingCurrentAddressId as PartnersExistingCurrentAddressId}
import identifiers.register.partnership.{PartnershipContactAddressChangedId, PartnershipContactAddressId, PartnershipContactDetailsChangedId, PartnershipPreviousAddressChangedId}
import identifiers.register.{DeclarationChangedId, DirectorsOrPartnersChangedId, MoreThanTenDirectorsOrPartnersChangedId}
import identifiers.{IndexId, UpdateModeId}
import models.requests.OptionalDataRequest
import models.{PSAUser, UpdateMode, UserType}
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HeaderCarrier
import utils.ViewPsaDetailsHelperSpec.readJsonFromFile
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.testhelpers.ViewPsaDetailsBuilder.{companyContactOnlyWithChangeLinks, companyWithChangeLinks, individualContactOnlyWithChangeLinks, individualWithChangeLinks, partnershipContactOnlyWithChangeLinks, partnershipWithChangeLinks}
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, PsaViewDetailsViewModel, SuperSection}
import org.mockito.Mockito.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PsaDetailsServiceSpec extends SpecBase with OptionValues with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  import PsaDetailsServiceSpec.*

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: OptionalDataRequest[?] = OptionalDataRequest(fakeRequest, "cacheId", PSAUser(UserType.Organisation, None, false, Some("test Psa id")),
    Some(UserAnswers(Json.obj()).set(DeclarationChangedId)(true).asOpt.value))

  override def beforeEach(): Unit = {
    reset(mockSubscriptionConnector, mockUserAnswersConnector, mockDataCompletion)
    when(mockDataCompletion.psaUpdateDetailsInCompleteAlert(any())).thenReturn(None)
  }

  private val mode = UpdateMode

  private val titlePsaDataFull = "psaDetails.title"
  private val titlePsaDataContactOnlyIndividual = "updateContactAddressCYA.individual.title"
  private val titlePsaDataContactOnlyCompany = "updateContactAddressCYA.company.title"
  private val titlePsaDataContactOnlyPartnership = "updateContactAddressCYA.partnership.title"

  "retrievePsaDataAndGenerateViewModel" must {

    "return the correct PSA individual view model with correct can de register flag and existing current address id" in {
      val expectedAddress = UserAnswers(individualUserAnswers).get(IndividualContactAddressId).get.toTolerantAddress

      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(individualUserAnswers))

      val result = service().retrievePsaDataAndGenerateViewModel
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(individualWithChangeLinks, "Stephen Wood", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataFull)
      }
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(ExistingCurrentAddressId).value mustBe expectedAddress
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(UpdateModeId).value mustBe true
    }

    "return the correct PSA company view model, also verify the correct existing current address ids and flags, and adviser Add links" in {
      val companyExpectedAddress = UserAnswers(companyUserAnswers).get(CompanyContactAddressId).get.toTolerantAddress
      val directorExpectedAddress = UserAnswers(companyUserAnswers).get(DirectorAddressId(0)).get.toTolerantAddress

      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(companyUserAnswers))

      val result = service().retrievePsaDataAndGenerateViewModel
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataFull)
      }

      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(CompanyExistingCurrentAddressId).value mustBe companyExpectedAddress
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(DirectorsExistingCurrentAddressId(0)).value mustBe directorExpectedAddress
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(UpdateModeId).value mustBe true
    }

    "return the correct PSA partnership view model, also correct existing current address ids and flags" in {
      val partnershipExpectedAddress = UserAnswers(partnershipUserAnswers).get(PartnershipContactAddressId).get.toTolerantAddress
      val partnerExpectedAddress = UserAnswers(partnershipUserAnswers).get(PartnerAddressId(0)).get.toTolerantAddress

      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(partnershipUserAnswers))
      when(mockDataCompletion.psaUpdateDetailsInCompleteAlert(any())).thenReturn(Some("incomplete.alert.message"))

      val result = service().retrievePsaDataAndGenerateViewModel
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(partnershipWithChangeLinks, "Test partnership name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message"), title = titlePsaDataFull)
      }
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(CompanyExistingCurrentAddressId).value mustBe partnershipExpectedAddress
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(PartnersExistingCurrentAddressId(0)).value mustBe partnerExpectedAddress
      UserAnswers(LocalFakeUserAnswersCacheConnector.lastUpsert.get).get(UpdateModeId).value mustBe true
    }

    "call psa subscription details to fetch data if no data is available in user answers" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)
      when(mockUserAnswersConnector.fetch(any(), any())).thenReturn(Future.successful(None))
      when(mockUserAnswersConnector.upsert(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateViewModel
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, never).removeAll(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetailsSelf(any(), any())
      }
    }

    "remove the existing data and call psa subscription details to fetch data if data for index is available in user answers" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)
      when(mockUserAnswersConnector.fetch(any(), any())).thenReturn(Future.successful(Some(Json.obj(IndexId.toString -> "index"))))
      when(mockUserAnswersConnector.upsert(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockUserAnswersConnector.removeAll(any(), any())).thenReturn(Future.successful(Ok))
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateViewModel
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, times(1)).removeAll(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetailsSelf(any(), any())
      }
    }

    "populate all the variations change flags when getUserAnswers is called when no user answers data" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)

      when(mockUserAnswersConnector.fetch(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).getUserAnswers
      whenReady(result) { userAnswers =>
        Seq(IndividualAddressChangedId,
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
        ).foreach(userAnswers.get(_) mustBe Some(false))
      }
    }
  }

  "retrievePsaDataAndGenerateContactDetailsOnlyViewModel" must {

    "return the correct PSA individual view model and page title" in {
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(individualUserAnswers))

      val result = service().retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(individualContactOnlyWithChangeLinks, "Stephen Wood", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataContactOnlyIndividual)
      }
    }

    "return the correct PSA company view model and page title" in {
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(companyUserAnswers))

      val result = service().retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(companyContactOnlyWithChangeLinks, "Test company name", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataContactOnlyCompany)
      }
    }

    "return the correct PSA partnership view model and page title" in {
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any()))
        .thenReturn(Future.successful(partnershipUserAnswers))
      when(mockDataCompletion.psaUpdateDetailsInCompleteAlert(any())).thenReturn(Some("incomplete.alert.message"))

      val result = service().retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(partnershipContactOnlyWithChangeLinks, "Test partnership name",
          isUserAnswerUpdated = false, userAnswersIncompleteMessage = None,
          title = titlePsaDataContactOnlyPartnership)
      }
    }

    "call psa subscription details to fetch data if no data is available in user answers" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)
      when(mockUserAnswersConnector.fetch(any(), any())).thenReturn(Future.successful(None))
      when(mockUserAnswersConnector.upsert(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, never).removeAll(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetailsSelf(any(), any())
      }
    }

    "remove the existing data and call psa subscription details to fetch data if data for index is available in user answers" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)
      when(mockUserAnswersConnector.fetch(any(), any())).thenReturn(Future.successful(Some(Json.obj(IndexId.toString -> "index"))))
      when(mockUserAnswersConnector.upsert(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockUserAnswersConnector.removeAll(any(), any())).thenReturn(Future.successful(Ok))
      when(mockSubscriptionConnector.getSubscriptionDetailsSelf(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, times(1)).removeAll(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetailsSelf(any(), any())
      }
    }
  }

}

object PsaDetailsServiceSpec extends SpecBase with MockitoSugar {

  private val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val mockUserAnswersConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockDataCompletion: DataCompletion = mock[DataCompletion]

  object LocalFakeUserAnswersCacheConnector extends FakeUserAnswersCacheConnector {
    override def fetch(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] =
      Future.successful(None)
  }

  def service(cacheConnector: UserAnswersCacheConnector = LocalFakeUserAnswersCacheConnector): PsaDetailServiceImpl = new PsaDetailServiceImpl(
    mockSubscriptionConnector,
    countryOptions,
    cacheConnector,
    mockDataCompletion
  )

  private val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  private val companyUserAnswers = readJsonFromFile("/data/psaCompanyUserAnswers.json")
  private val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")

  val individualSuperSections: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("cya.label.dob", Seq("29/03/1947"), false, None),
            AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false, None),
            AnswerRow("email.label", Seq("aaa@aa.com"), false, None),
            AnswerRow("phone.label", Seq("0044-09876542312"), false, None))))),

    SuperSection(
      Some("pensionAdvisor.section.header"),
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("adviserName.heading", Seq("Pension Advisor"), false, None),
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false, None),
            AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"), false, None))))))
  val organisationSuperSections: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("vat.label", Seq("12345678"), false, None),
            AnswerRow("paye.label", Seq("9876543210"), false, None),
            AnswerRow("crn.label", Seq("1234567890"), false, None),
            AnswerRow("utr.label", Seq("121414151"), false, None),
            AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false, None),
            AnswerRow("company.email.label", Seq("aaa@aa.com"), false, None),
            AnswerRow("company.phone.label", Seq("0044-09876542312"), false, None))))),
    SuperSection(
      Some("director.supersection.header"),
      Seq(
        AnswerSection(
          Some("abcdef dfgdsfff dfgfdgfdg"),
          Seq(
            AnswerRow("cya.label.dob", Seq("1950-03-29"), false, None),
            AnswerRow("utr.label", Seq("1234567892"), false, None),
            AnswerRow("cya.label.address", Seq("addressline1,", "addressline2,", "addressline3,", "addressline4,", "B5 9EX,", "Country of GB"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("line1,", "line2,", "line3,", "line4,", "567253,", "Country of AD"), false, None),
            AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false, None),
            AnswerRow("phone.label", Seq("0044-09876542312"), false, None))),
        AnswerSection(
          Some("sdfdff sdfdsfsdf dfdsfsf"),
          Seq(
            AnswerRow("cya.label.dob", Seq("1950-07-29"), false, None),
            AnswerRow("utr.label", Seq("7897700000"), false, None),
            AnswerRow("cya.label.address", Seq("fgfdgdfgfd,", "dfgfdgdfg,", "fdrtetegfdgdg,", "dfgfdgdfg,", "56546,", "Country of AD"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("werrertqe,", "ereretfdg,", "asafafg,", "fgdgdasdf,", "23424,", "Country of AD"), false, None),
            AnswerRow("email.label", Seq("aaa@gmail.com"), false, None),
            AnswerRow("phone.label", Seq("0044-09876542334"), false, None))))),
    SuperSection(
      Some("pensionAdvisor.section.header"),
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("adviserName.heading", Seq("Pension Advisor"), false, None),
            AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"), false, None),
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false, None))))))
}


