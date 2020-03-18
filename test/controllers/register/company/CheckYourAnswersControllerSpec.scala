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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import models._
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val defaultCompany = Message("theBusiness").resolve
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val crn = "test reg no"
  private val vat = "Test Vat"
  private val paye = "Test Paye"
  private val addressYears = AddressYears.OverAYear
  private val email = "test@email"
  private val phone = "123"
  private val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )
  val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  val contactDetailsHeading = "common.checkYourAnswers.contact.details.heading"

  private val completeUserAnswers = UserAnswers().businessName().businessUtr().companyHasCrn(hasCrn = true).
    companyCrn(crn).hasPaye(flag = true).enterPaye(paye).hasVatRegistrationNumber(flag = true).enterVat(vat).
    companyContactAddress(address).companyAddressYears(addressYears).companyPreviousAddress(address).
    companyEmail(email).companyPhone(phone)


  "CheckYourAnswers Controller" when {

    "on a GET" must {

      "render the view correctly for all the rows of answer section" in {
        val retrievalAction = completeUserAnswers.dataRetrievalAction
        val rows = Seq(
          answerRow(
            Message("businessName.heading", Message("businessType.limitedCompany").resolve.toLowerCase()).resolve,
            Seq("test company")
          ),
          answerRow(
            Message("utr.heading", Message("businessType.limitedCompany").resolve.toLowerCase()).resolve,
            Seq("1111111111")
          ),
          answerRow(
            messages("hasCompanyNumber.heading", defaultCompany), Seq("site.yes"), answerIsMessageKey = true,
            Some(Link(controllers.register.company.routes.HasCompanyCRNController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasCompanyNumber.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            messages("companyRegistrationNumber.heading", defaultCompany), Seq(crn), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("companyRegistrationNumber.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            Message("hasPAYE.heading", defaultCompany),
            Seq("site.yes"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(controllers.register.company.routes.HasCompanyPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            Message("enterPAYE.heading", defaultCompany),
            Seq(paye),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            label = Message("hasVAT.heading", defaultCompany),
            Seq("site.yes"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(controllers.register.company.routes.HasCompanyVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasVAT.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            label = Message("enterVAT.heading", defaultCompany),
            Seq(vat),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyEnterVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterVAT.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            Message("cya.label.contact.address", defaultCompany),
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            Message("addressYears.heading", Message("theBusiness").resolve),
            Seq(s"common.addressYears.${addressYears.toString}"), answerIsMessageKey = true,
            Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", Message("theBusiness").resolve))
          ),
          answerRow(
            Message("previousAddress.checkYourAnswersLabel", defaultCompany),
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            label = messages("email.title", defaultCompany),
            answer = Seq(email),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyEmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultCompany))
          ),
          answerRow(
            label = messages("phone.title", defaultCompany),
            answer = Seq(phone),
            changeUrl = Some(Link(controllers.register.company.routes.CompanyPhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultCompany))
          )
        )
        val sections = Seq(AnswerSection(None, rows))
        testRenderedView(sections, retrievalAction)
      }

      "redirect to session expired page when there is no data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST" must {
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

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      countryOptions,
      stubMessagesControllerComponents(),
      view
    )

  private def call: Call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }


  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)
    val expectedResult = view(
      sections,
      call,
      None,
      NormalMode,
      Nil
    )(fakeRequest, messages).toString()

    status(result) mustBe OK
    contentAsString(result) mustBe expectedResult
  }
}
