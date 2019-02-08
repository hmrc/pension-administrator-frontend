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

import config.FeatureSwitchManagementServiceTestImpl
import connectors.{DeRegistrationConnector, FakeUserAnswersCacheConnector, SubscriptionConnector}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAllowAccessProvider, FakeDataRetrievalAction}
import identifiers.PsaId
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UpdateMode, UserType}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.{contentAsString, status, _}
import utils.FakeCountryOptions
import utils.Toggles._
import utils.ViewPsaDetailsHelperSpec.readJsonFromFile
import utils.countryOptions.CountryOptions
import utils.testhelpers.PsaSubscriptionBuilder._
import utils.testhelpers.ViewPsaDetailsBuilder._
import viewmodels.{AnswerRow, AnswerSection, PsaViewDetailsViewModel, SuperSection}
import views.html.psa_details

import scala.concurrent.Future

class PsaDetailsControllerSpec extends ControllerSpecBase {

  import PsaDetailsControllerSpec._

  "Psa details Controller" must {
    "when variations and dergistration are disabled" when {
      "return 200 and  correct view for a GET for PSA individual" in {
        featureSwitchManagementService.change(isVariationsEnabled, false)
        featureSwitchManagementService.change(isDeregistrationEnabled, false)
        when(subscriptionConnector.getSubscriptionModel(any())(any(), any()))
          .thenReturn(Future.successful(psaSubscriptionIndividual))
        val result = controller(userType = UserType.Individual).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(individualSuperSections, "Stephen Wood", false)
      }

      "return 200 and  correct view for a GET for PSA company" in {

        when(subscriptionConnector.getSubscriptionModel(any())(any(), any()))
          .thenReturn(Future.successful(psaSubscriptionCompany))
        val result = controller(userType = UserType.Organisation).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(organisationSuperSections, "Test company name", false)
      }
    }

    "when variations and deregistration are enabled" when {
      "return 200 and  correct view for a GET for PSA individual" in {
        featureSwitchManagementService.change(isVariationsEnabled, true)
        featureSwitchManagementService.change(isDeregistrationEnabled, true)

        when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(individualUserAnswers))

        when(deregistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )

        val result = controller(validDataIndividual, userType = UserType.Individual).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(individualWithChangeLinks, "Stephen Wood", true)
        (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }
      "return 200 and  correct view for a GET for PSA company" in {

        when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(companyUserAnswers))

        when(deregistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )

        val result = controller(validDataCompany, userType = UserType.Organisation).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyWithChangeLinks, "Test company name", true)
        (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }

      "return 200 and  correct view for a GET for PSA partnership" in {

        when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
          .thenReturn(Future.successful(partnershipUserAnswers))

        when(deregistrationConnector.canDeRegister(any())(any(), any())).thenReturn(
          Future.successful(true)
        )

        val result = controller(validDataPartnership, userType = UserType.Organisation).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipWithChangeLinks, "Test partnership name", true)
        (FakeUserAnswersCacheConnector.lastUpsert.get \ "updateMode").get mustBe JsBoolean(true)
      }
    }
  }
}

object PsaDetailsControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val externalId = "test-external-id"

  private val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  private val companyUserAnswers = readJsonFromFile("/data/psaCompanyUserAnswers.json")
  private val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")

  class FakeAuthAction(userType: UserType) extends AuthAction {
    override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, externalId, PSAUser(userType, None, false, None, Some("test Psa id"))))
  }

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val name = "testName"

  private val subscriptionConnector = mock[SubscriptionConnector]
  private val deregistrationConnector = mock[DeRegistrationConnector]

  val validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      PsaId.toString ->
        "S1234567890"
    )
  )
  )

  val validDataIndividual: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(individualUserAnswers))
  val validDataCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(companyUserAnswers))
  val validDataPartnership: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(partnershipUserAnswers))

  private val config = app.injector.instanceOf[Configuration]
  val featureSwitchManagementService = new FeatureSwitchManagementServiceTestImpl(config, environment)

  def controller(dataRetrievalAction: DataRetrievalAction = validData, userType: UserType) =
    new PsaDetailsController(
      frontendAppConfig,
      messagesApi,
      new FakeAuthAction(userType),
      FakeAllowAccessProvider(),
      subscriptionConnector,
      deregistrationConnector,
      FakeUserAnswersCacheConnector,
      countryOptions,
      featureSwitchManagementService
    )

  private def viewAsString(superSections: Seq[SuperSection] = Seq.empty, name: String = "",
                           canDeregister: Boolean = true, isUserAnswerUpdated: Boolean = false) = {

    val model = PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated,  canDeregister)
    psa_details(frontendAppConfig, model)(fakeRequest, messages).toString
  }

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


  val organisationSuperSections =
    Seq(
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