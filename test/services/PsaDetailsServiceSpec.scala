/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.register.company.directors.{DirectorAddressId, ExistingCurrentAddressId => DirectorsExistingCurrentAddressId}
import identifiers.register.company.{CompanyContactAddressId, CompanyPreviousAddressChangedId, CompanyContactDetailsChangedId, CompanyContactAddressChangedId, ExistingCurrentAddressId => CompanyExistingCurrentAddressId}
import identifiers.register.individual._
import identifiers.register.partnership.partners.{PartnerAddressId, ExistingCurrentAddressId => PartnersExistingCurrentAddressId}
import identifiers.register.partnership.{PartnershipContactDetailsChangedId, PartnershipContactAddressChangedId, PartnershipPreviousAddressChangedId, PartnershipContactAddressId}
import identifiers.register.{DirectorsOrPartnersChangedId, DeclarationChangedId, MoreThanTenDirectorsOrPartnersChangedId}
import identifiers.{UpdateModeId, IndexId}
import models.requests.OptionalDataRequest
import models.{UserType, PSAUser, UpdateMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{OptionValues, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HeaderCarrier
import utils.ViewPsaDetailsHelperSpec.readJsonFromFile
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.testhelpers.ViewPsaDetailsBuilder.{companyContactOnlyWithChangeLinks, individualContactOnlyWithChangeLinks, partnershipContactOnlyWithChangeLinks, companyWithChangeLinks, individualWithChangeLinks, partnershipWithChangeLinks}
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerSection, PsaViewDetailsViewModel, SuperSection, AnswerRow}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, ExecutionContext}

class PsaDetailsServiceSpec extends SpecBase with OptionValues with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  import PsaDetailsServiceSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: OptionalDataRequest[_] = OptionalDataRequest(fakeRequest, "cacheId", PSAUser(UserType.Organisation, None, false, Some("test Psa id")),
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

        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(individualUserAnswers))

        val result = service().retrievePsaDataAndGenerateViewModel("123")
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

        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(companyUserAnswers))

        val result = service().retrievePsaDataAndGenerateViewModel("123")
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

        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(partnershipUserAnswers))
        when(mockDataCompletion.psaUpdateDetailsInCompleteAlert(any())).thenReturn(Some("incomplete.alert.message"))

        val result = service().retrievePsaDataAndGenerateViewModel("123")
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
        when(mockUserAnswersConnector.fetch(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockUserAnswersConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

        val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateViewModel("123")
        whenReady(result) { _ =>
          verify(mockUserAnswersConnector, never()).removeAll(any())(any(), any())
          verify(mockSubscriptionConnector, times(1)).getSubscriptionDetails(any())(any(), any())
        }
      }

      "remove the existing data and call psa subscription details to fetch data if data for index is available in user answers" in {
        reset(mockUserAnswersConnector, mockSubscriptionConnector)
        when(mockUserAnswersConnector.fetch(any())(any(), any())).thenReturn(Future.successful(Some(Json.obj(IndexId.toString -> "index"))))
        when(mockUserAnswersConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
        when(mockUserAnswersConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

        val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateViewModel("123")
        whenReady(result) { _ =>
          verify(mockUserAnswersConnector, times(1)).removeAll(any())(any(), any())
          verify(mockSubscriptionConnector, times(1)).getSubscriptionDetails(any())(any(), any())
        }
      }

      "populate all the variations change flags when getUserAnswers is called when no user answers data" in {
        reset(mockUserAnswersConnector, mockSubscriptionConnector)

        when(mockUserAnswersConnector.fetch(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

        val result = service(mockUserAnswersConnector).getUserAnswers("123", request.externalId)
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
      when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
        .thenReturn(Future.successful(individualUserAnswers))

      val result = service().retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(individualContactOnlyWithChangeLinks, "Stephen Wood", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataContactOnlyIndividual)
      }
    }

    "return the correct PSA company view model and page title" in {
      when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
        .thenReturn(Future.successful(companyUserAnswers))

      val result = service().retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) {
        _ mustBe PsaViewDetailsViewModel(companyContactOnlyWithChangeLinks, "Test company name", isUserAnswerUpdated = false,
          userAnswersIncompleteMessage = None, title = titlePsaDataContactOnlyCompany)
      }
    }

    "return the correct PSA partnership view model and page title" in {
      when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
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
      when(mockUserAnswersConnector.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockUserAnswersConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, never()).removeAll(any())(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetails(any())(any(), any())
      }
    }

    "remove the existing data and call psa subscription details to fetch data if data for index is available in user answers" in {
      reset(mockUserAnswersConnector, mockSubscriptionConnector)
      when(mockUserAnswersConnector.fetch(any())(any(), any())).thenReturn(Future.successful(Some(Json.obj(IndexId.toString -> "index"))))
      when(mockUserAnswersConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockUserAnswersConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any())).thenReturn(Future.successful(partnershipUserAnswers))

      val result = service(mockUserAnswersConnector).retrievePsaDataAndGenerateContactDetailsOnlyViewModel("A2100005", mode)
      whenReady(result) { _ =>
        verify(mockUserAnswersConnector, times(1)).removeAll(any())(any(), any())
        verify(mockSubscriptionConnector, times(1)).getSubscriptionDetails(any())(any(), any())
      }
    }
  }

}

object PsaDetailsServiceSpec extends SpecBase with MockitoSugar {

  private val mockSubscriptionConnector = mock[SubscriptionConnector]
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val mockUserAnswersConnector = mock[UserAnswersCacheConnector]
  private val mockDataCompletion = mock[DataCompletion]

  object LocalFakeUserAnswersCacheConnector extends FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        executionContext: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = {

      Future.successful(None)
    }
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
            AnswerRow("common.nino", Seq("AA999999A"), false, None),
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
            AnswerRow("common.nino", Seq("AA999999A"), false, None),
            AnswerRow("utr.label", Seq("1234567892"), false, None),
            AnswerRow("cya.label.address", Seq("addressline1,", "addressline2,", "addressline3,", "addressline4,", "B5 9EX,", "Country of GB"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("line1,", "line2,", "line3,", "line4,", "567253,", "Country of AD"), false, None),
            AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false, None),
            AnswerRow("phone.label", Seq("0044-09876542312"), false, None))),
        AnswerSection(
          Some("sdfdff sdfdsfsdf dfdsfsf"),
          Seq(
            AnswerRow("cya.label.dob", Seq("1950-07-29"), false, None),
            AnswerRow("common.nino", Seq("AA999999A"), false, None),
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


