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

package service

import base.SpecBase
import config.FeatureSwitchManagementServiceTestImpl
import connectors.{DeRegistrationConnector, FakeUserAnswersCacheConnector, SubscriptionConnector, UserAnswersCacheConnector}
import identifiers.UpdateModeId
import identifiers.register.company.directors.IsDirectorCompleteId
import identifiers.register.partnership.partners.IsPartnerCompleteId
import models.requests.AuthenticatedRequest
import models.{PSAUser, UpdateMode, UserType}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.JsBoolean
import play.api.test.Helpers.{contentAsString, status}
import services.PsaDetailServiceImpl
import uk.gov.hmrc.http.HeaderCarrier
import utils.Toggles.{isDeregistrationEnabled, isVariationsEnabled}
import utils.ViewPsaDetailsHelperSpec.readJsonFromFile
import utils.countryOptions.CountryOptions
import utils.testhelpers.PsaSubscriptionBuilder.{psaSubscriptionCompany, psaSubscriptionIndividual}
import utils.testhelpers.ViewPsaDetailsBuilder.{companyWithChangeLinks, individualWithChangeLinks, partnershipWithChangeLinks}
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, PsaViewDetailsViewModel, SuperSection}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class PsaDetailsServiceSpec extends SpecBase with OptionValues with MockitoSugar with ScalaFutures {

  import PsaDetailsServiceSpec._


  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request = AuthenticatedRequest(fakeRequest, "", PSAUser(UserType.Organisation, None, false, Some("test Psa id")))


  "PsaDetailsService" must {
    "when variations and dergistration are disabled" when {
      "return 200 and  correct view for a GET for PSA individual" in {
        fs.change(isVariationsEnabled, false)
        fs.change(isDeregistrationEnabled, false)
        when(mockSubscriptionConnector.getSubscriptionModel(any())(any(), any()))
          .thenReturn(Future.successful(psaSubscriptionIndividual))

        val result  = service.retrievePsaDataAndGenerateViewModel("123")

        whenReady(result) { _ mustBe PsaViewDetailsViewModel(individualSuperSections, "Stephen Wood", false, false)}

      }

      "return 200 and  correct view for a GET for PSA company" in {

        when(mockSubscriptionConnector.getSubscriptionModel(any())(any(), any()))
          .thenReturn(Future.successful(psaSubscriptionCompany))


        val result  = service.retrievePsaDataAndGenerateViewModel("123")
          whenReady(result) { _  mustBe PsaViewDetailsViewModel(organisationSuperSections, "Test company name", false, false)}
      }
    }

    "when variations and deregistration are enabled" when {
      "return 200 and  correct view for a GET for PSA individual" in {
        fs.change(isVariationsEnabled, true)
        fs.change(isDeregistrationEnabled, true)

        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(individualUserAnswers))

        when(mockDeRegistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )
        val result  = service.retrievePsaDataAndGenerateViewModel("123")
          whenReady(result) { _  mustBe PsaViewDetailsViewModel(individualWithChangeLinks, "Stephen Wood", false, true)}
     //   (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }

      "return 200 and  correct view for a GET for PSA company" in {
        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(companyUserAnswers))

        when(mockDeRegistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )

        val result  = service.retrievePsaDataAndGenerateViewModel("123")
        whenReady(result) { _  mustBe PsaViewDetailsViewModel(companyWithChangeLinks, "Test company name", false, true)}

//        UserAnswers(FakeUserAnswersCacheConnector.lastUpsert.get).get(IsDirectorCompleteId(0)).value mustBe true
//        UserAnswers(FakeUserAnswersCacheConnector.lastUpsert.get).get(UpdateModeId).value mustBe true
//        (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }

      "return 200 and  correct view for a GET for PSA partnership" in {
        when(mockSubscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(partnershipUserAnswers))

        when(mockDeRegistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )

        val result  = service.retrievePsaDataAndGenerateViewModel("123")
          whenReady(result) { _  mustBe PsaViewDetailsViewModel(partnershipWithChangeLinks, "Test partnership name", false, true)}

//        UserAnswers(FakeUserAnswersCacheConnector.lastUpsert.get).get(IsPartnerCompleteId(0)).value mustBe true
//        UserAnswers(FakeUserAnswersCacheConnector.lastUpsert.get).get(UpdateModeId).value mustBe true
//        (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }
    }
  }
}
object PsaDetailsServiceSpec extends SpecBase with MockitoSugar {

  val configuration = injector.instanceOf[Configuration]
  val fs = new FeatureSwitchManagementServiceTestImpl(configuration, environment)
  val mockSubscriptionConnector = mock[SubscriptionConnector]
  val mockDeRegistrationConnector = mock[DeRegistrationConnector]
  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  def service = new PsaDetailServiceImpl(
    fs,
    messagesApi,
    mockSubscriptionConnector,
    countryOptions,
    mockDeRegistrationConnector,
    FakeUserAnswersCacheConnector
  )

  val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  val companyUserAnswers = readJsonFromFile("/data/psaCompanyUserAnswers.json")
  val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")


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
            AnswerRow("Has Stephen Wood been at their address for more than 12 months?", Seq("No"), false, None),
            AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false, None),
            AnswerRow("email.label", Seq("aaa@aa.com"), false, None),
            AnswerRow("phone.label", Seq("0044-09876542312"), false, None))))),

    SuperSection(
      Some("pensionAdvisor.section.header"),
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("pensions.advisor.label", Seq("Pension Advisor"), false, None),
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
            AnswerRow("Has Test company name been at their address for more than 12 months?", Seq("No"), false, None),
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
            AnswerRow("pensions.advisor.label", Seq("Pension Advisor"), false, None),
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false, None),
            AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"), false, None))))))

}


