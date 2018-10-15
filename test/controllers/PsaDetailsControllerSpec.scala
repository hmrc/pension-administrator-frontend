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

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.PsaId
import models.requests.DataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers._
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import utils.countryOptions.CountryOptions
import utils.testhelpers.PsaSubscriptionBuilder._
import utils.{FakeCountryOptions, PsaDetailsHelper}
import viewmodels.{AnswerRow, AnswerSection, SuperSection}
import views.html.psa_details

import scala.concurrent.Future

class PsaDetailsControllerSpec extends ControllerSpecBase {
  import PsaDetailsControllerSpec._

  "Psa details Controller" must {
    "return 200 and  correct view for a GET for PSA individual" in {

      when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
        .thenReturn(Future.successful(psaSubscriptionIndividual))
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individualSuperSections, "abcdefghijkl abcdefghijkl abcdefjkl")
    }

    "return 200 and  correct view for a GET for PSA company" in {

      when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
        .thenReturn(Future.successful(psaSubscriptionCompany))
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(organisationSuperSections, "Test company name")
    }

    "redirect to Session Expired for a GET if not existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

object PsaDetailsControllerSpec extends ControllerSpecBase with  MockitoSugar {
  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val name = "testName"

  def call: Call = controllers.routes.CheckYourAnswersController.onSubmit()
  private val subscriptionConnector = mock[SubscriptionConnector]

  val validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      PsaId.toString ->
        "S1234567890"
    )
  )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new PsaDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      subscriptionConnector,
      countryOptions
    )


  private def viewAsString(superSections: Seq[SuperSection] = Seq.empty, name: String = "") =
    psa_details(frontendAppConfig, superSections, name)(fakeRequest, messages).toString


  val individualSuperSections: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("cya.label.dob",Seq("29/03/1947"),false,None),
            AnswerRow("common.nino",Seq("AA999999A"),false,None),
            AnswerRow("cya.label.address",Seq("Telford1,", "Telford2,", "Telford3,", "Telford3,", "TF3 4ER,", "Country of GB"),false,None),
            AnswerRow("Has abcdefghijkl abcdefghijkl abcdefjkl been at their address for more than 12 months?",Seq("No"),false,None),
            AnswerRow("common.previousAddress.checkyouranswers",Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"),false,None),
            AnswerRow("email.label",Seq("aaa@aa.com"),false,None),
            AnswerRow("phone.label",Seq("0044-09876542312"),false,None))))),

    SuperSection(
      Some("pensionAdvisor.section.header"),
      Seq(
        AnswerSection(
          None,
          Seq(
            AnswerRow("pensions.advisor.label",Seq("sgfdgssd"),false,None),
            AnswerRow("contactDetails.email.checkYourAnswersLabel",Seq("aaa@yahoo.com"),false,None),
            AnswerRow("cya.label.address",Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"),false,None))))))

  
  val organisationSuperSections =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            Seq(
              AnswerRow("vat.label",Seq("12345678"),false,None),
              AnswerRow("paye.label",Seq("9876543210"),false,None),
              AnswerRow("crn.label",Seq("1234567890"),false,None),
              AnswerRow("utr.label",Seq("121414151"),false,None),
              AnswerRow("company.address.label",Seq("Telford1,", "Telford2,", "Telford3,", "Telford3,", "TF3 4ER,", "Country of GB"),false,None),
              AnswerRow("Has Test company name been at their address for more than 12 months?",Seq("No"),false,None),
              AnswerRow("common.previousAddress.checkyouranswers",Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"),false,None),
              AnswerRow("company.email.label",Seq("aaa@aa.com"),false,None),
              AnswerRow("company.phone.label",Seq("0044-09876542312"),false,None))))),
        SuperSection(
          Some("director.supersection.header"),
          Seq(
            AnswerSection(
              Some("abcdef dfgdsfff dfgfdgfdg"),
              Seq(
                AnswerRow("cya.label.dob",Seq("1950-03-29"),false,None),
                AnswerRow("common.nino",Seq("AA999999A"),false,None),
                AnswerRow("utr.label",Seq("1234567892"),false,None),
                AnswerRow("cya.label.address",Seq("addressline1,", "addressline2,", "addressline3,", "addressline4,", "B5 9EX,", "Country of GB"),false,None),
                AnswerRow("common.previousAddress.checkyouranswers",Seq("line1,", "line2,", "line3,", "line4,", "567253,", "Country of AD"),false,None),
                AnswerRow("email.label",Seq("abc@hmrc.gsi.gov.uk"),false,None),
                AnswerRow("phone.label",Seq("0044-09876542312"),false,None))),
            AnswerSection(
              Some("sdfdff sdfdsfsdf dfdsfsf"),
              Seq(
                AnswerRow("cya.label.dob",Seq("1950-07-29"),false,None),
                AnswerRow("common.nino",Seq("AA999999A"),false,None),
                AnswerRow("utr.label",Seq("7897700000"),false,None),
                AnswerRow("cya.label.address",Seq("fgfdgdfgfd,", "dfgfdgdfg,", "fdrtetegfdgdg,", "dfgfdgdfg,", "56546,", "Country of AD"),false,None),
                AnswerRow("common.previousAddress.checkyouranswers",Seq("werrertqe,", "ereretfdg,", "asafafg,", "fgdgdasdf,", "23424,", "Country of AD"),false,None),
                AnswerRow("email.label",Seq("aaa@gmail.com"),false,None),
                AnswerRow("phone.label",Seq("0044-09876542334"),false,None))))),
        SuperSection(
          Some("pensionAdvisor.section.header"),
          Seq(
            AnswerSection(
              None,
              Seq(
                AnswerRow("pensions.advisor.label",Seq("sgfdgssd"),false,None),
                AnswerRow("contactDetails.email.checkYourAnswersLabel",Seq("aaa@yahoo.com"),false,None),
                AnswerRow("cya.label.address",Seq("addline1,", "addline2,", "addline3,", "addline4 ,", "56765,", "Country of AD"),false,None))))))

}