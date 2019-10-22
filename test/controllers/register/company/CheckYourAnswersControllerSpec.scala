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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId}
import identifiers.register.company.{PhoneId, _}
import identifiers.register.{EnterPAYEId, EnterVATId, HasPAYEId, HasVATId}
import models.RegistrationLegalStatus.LimitedCompany
import models._
import models.register.BusinessType
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._


  "CheckYourAnswers Controller" when {

    "on a GET request for Company Details section " must {

      "render the view correctly for the company name and utr" in {
        val rows = Seq(answerRow(Message("businessName.heading",
          Message("businessType.limitedCompany").resolve.toLowerCase()).resolve, Seq("Test Company Name")),
          answerRow(Message("utr.heading",
            Message("businessType.limitedCompany").resolve.toLowerCase()).resolve, Seq("Test UTR")))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          BusinessTypeId.toString -> BusinessType.LimitedCompany.toString,
          BusinessNameId.toString -> "Test Company Name",
          BusinessUTRId.toString -> "Test UTR")

        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for vat registration number" in {
        val rows = Seq(
          answerRow(label = Message("hasVAT.heading", defaultCompany), Seq("site.yes"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.HasCompanyVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasVAT.visuallyHidden.text", defaultCompany))),
          answerRow(label = Message("enterVAT.heading", defaultCompany), Seq("Test Vat"), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyEnterVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterVAT.visuallyHidden.text", defaultCompany))))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          HasVATId.toString -> true,
          EnterVATId.toString -> "Test Vat"
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for paye number" in {
        val rows = Seq(answerRow(Message("hasPAYE.heading", defaultCompany), Seq("site.yes"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.HasCompanyPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", defaultCompany))),
          answerRow(Message("enterPAYE.heading", defaultCompany), Seq("Test Paye"), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", defaultCompany))))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          HasPAYEId.toString -> true,
          EnterPAYEId.toString -> "Test Paye"
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for company registration number" in {
        val rows = Seq(
          answerRow(
            messages("companyRegistrationNumber.heading", defaultCompany), Seq("test reg no"), false,
            Some(Link(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("companyRegistrationNumber.visuallyHidden.text", defaultCompany))
          )
        )

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          CompanyRegistrationNumberId.toString -> "test reg no"
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for has company number" in {
        val rows = Seq(
          answerRow(
            messages("hasCompanyNumber.heading", defaultCompany), Seq("site.yes"), true,
            Some(Link(controllers.register.company.routes.HasCompanyCRNController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasCompanyNumber.visuallyHidden.text", defaultCompany))
          )
        )

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          HasCompanyCRNId.toString -> true
        )
        testRenderedView(sections, retrievalAction)
      }
    }

    "on a GET request for Company Contact Details section " must {

      "render the view correctly for the company contact address" in {
        val rows = Seq(answerRow(Message("cya.label.company.contact.address", defaultCompany),
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          ),
          answerIsMessageKey = false,
          Some(Link(controllers.register.company.routes.CompanyContactAddressController.onPageLoad(CheckMode).url)),
          visuallyHiddenLabel = Some(Message("companyContactAddress.visuallyHidden.text", defaultCompany))))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          CompanyContactAddressId.toString -> address
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the company address years" in {
        val addressYears = AddressYears.OverAYear
        val rows = Seq(answerRow(Message("addressYears.heading", Message("theCompany").resolve),
          Seq(s"common.addressYears.${addressYears.toString}"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
          visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", Message("theCompany").resolve))))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          CompanyAddressYearsId.toString -> addressYears.toString
        )
        testRenderedView(sections, retrievalAction)
      }

      "render the view correctly for the company previous address" in {
        val address = Address(
          "address-line-1",
          "address-line-2",
          None,
          None,
          Some("post-code"),
          "country"
        )
        val rows = Seq(answerRow(Message("companyPreviousAddress.checkYourAnswersLabel", defaultCompany),
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          ), answerIsMessageKey = false,
          Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)),
          visuallyHiddenLabel = Some(Message("companyPreviousAddress.visuallyHidden.text", defaultCompany))))

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          CompanyPreviousAddressId.toString -> address
        )
        testRenderedView(sections, retrievalAction)
      }
    }

    "on a GET request for Contact Details section " must {

      "render the view correctly for email and phone" in {
        val rows = Seq(
          answerRow(
            label = messages("email.title", defaultCompany),
            answer = Seq("test@email"),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyEmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            label = messages("phone.title", defaultCompany),
            answer = Seq("1234567890"),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyPhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultCompany))
          )
        )

        val sections = answerSections(None, rows)

        val retrievalAction = dataRetrievalAction(
          "contactDetails" -> Json.obj(
            PhoneId.toString -> "1234567890",
            EmailId.toString -> "test@email"
          )
        )

        testRenderedView(
          sections = sections, dataRetrievalAction = retrievalAction
        )
      }
    }

    "on a GET request with no existing data" must {
      "redirect to session expired page" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST request" must {
      "redirect to the next page" in {
        val result = controller().onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Session expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val companyName = "Test Company Name"
  private val defaultCompany = Message("theCompany").resolve
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val contactDetailsHeading = "common.checkYourAnswers.contact.details.heading"

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      countryOptions
    )

  private def companyDetails(row: Seq[AnswerRow] = Seq.empty) = AnswerSection(
    None,
    row
  )

  private val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  private def call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message]= None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }


  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {

    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

    val expectedResult = check_your_answers(
      frontendAppConfig,
      sections,
      call,
      None,
      NormalMode
    )(fakeRequest, messages).toString()

    status(result) mustBe OK

    contentAsString(result) mustBe expectedResult
  }
}
