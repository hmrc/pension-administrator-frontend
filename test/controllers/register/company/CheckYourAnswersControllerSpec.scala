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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import models._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isCompanyDetailsComplete(any())).thenReturn(true)
  }

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val mockDataCompletion = mock[DataCompletion]
  private val defaultCompany = "test company"
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val crn = "test-crn"
  private val vat = "test-vat"
  private val paye = "test-paye"
  private val addressYears = AddressYears.UnderAYear
  private val email = "test@test.com"
  private val phone = "111"
  private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "Country of GB")
  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new CheckYourAnswersController(
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockDataCompletion,
      new FakeNavigator(desiredRoute = onwardRoute),
      countryOptions,
      controllerComponents,
      view
    )

  private def call: Call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  private def answerRow(
                         label: String,
                         answer: Seq[String],
                         answerIsMessageKey: Boolean = false,
                         changeUrl: Option[Link] = None,
                         visuallyHiddenLabel: Option[Message] = None
                       ): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(
                                sections: Seq[AnswerSection],
                                result: Future[Result],
                                isComplete: Boolean = true
                              ): Unit = {

    val expectedResult = view(
      answerSections = sections,
      postUrl = call,
      psaNameOpt = None,
      mode = NormalMode,
      isComplete = isComplete
    )(fakeRequest, messages).toString()

    status(result) mustBe OK

    contentAsString(result) mustBe expectedResult
  }

  private val answerRows = Seq(
    answerRow(
      label = Message("businessName.heading", Message("businessType.limitedCompany.lc").resolve),
      answer = Seq("test company")
    ),
    answerRow(
      label = Message("utr.heading", Message("theCompany"), Message("utr.company.hint")),
      answer = Seq("1111111111")
    ),
    answerRow(
      label = messages("hasCompanyNumber.heading", defaultCompany),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(controllers.register.company.routes.HasCompanyCRNController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("hasCompanyNumber.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = messages("companyRegistrationNumber.heading", defaultCompany),
      answer = Seq(crn),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("companyRegistrationNumber.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("hasPAYE.heading", defaultCompany),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(controllers.register.company.routes.HasCompanyPAYEController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("enterPAYE.heading", defaultCompany),
      answer = Seq(paye),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("hasVAT.heading", defaultCompany),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(controllers.register.company.routes.HasCompanyVATController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("hasVAT.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("enterVAT.heading", defaultCompany),
      answer = Seq(vat),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyEnterVATController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("enterVAT.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("cya.label.contact.address", defaultCompany),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("addressYears.heading", defaultCompany),
      answer = Seq(s"common.addressYears.${addressYears.toString}"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("previousAddress.checkYourAnswersLabel", defaultCompany),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
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

  private val answerRowsNonUK = Seq(
    answerRow(
      label = Message("businessName.heading", Message("businessType.limitedCompany.lc")),
      answer = Seq("test company")
    ),
    answerRow(
      label = Message("cya.label.contact.address", defaultCompany),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("addressYears.heading", defaultCompany),
      answer = Seq(s"common.addressYears.${addressYears.toString}"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultCompany))
    ),
    answerRow(
      label = Message("previousAddress.checkYourAnswersLabel", defaultCompany),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
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

  "CheckYourAnswers Controller" when {

    "on a GET" must {

      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
        val retrievalAction = UserAnswers().completeCompanyDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad(NormalMode)(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows))
        testRenderedView(sections, result)
      }

      "render the view correctly for all the rows of answer section if business name and address is present for NON UK" in {
        val retrievalAction = UserAnswers().completeCompanyDetailsNonUK.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad(NormalMode)(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRowsNonUK))
        testRenderedView(sections, result)
      }

      "redirect to register as business page when business name, registration info and utr is not present for UK" in {
        val result =
          controller(UserAnswers().areYouInUk(answer = true).dataRetrievalAction)
            .onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }

      "redirect to register as business page when business name, registration info and address is not present for NON UK" in {
        val result =
          controller(UserAnswers().areYouInUk(answer = false).dataRetrievalAction)
            .onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }

      "redirect to session expired page when there is no data" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }

    "on a POST" must {
      "redirect to the next page when data is complete" in {
        val retrievalAction = UserAnswers().completeCompanyDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "load the same cya page when data is not complete" in {
        when(mockDataCompletion.isCompanyDetailsComplete(any())).thenReturn(false)

        val retrievalAction = UserAnswers().completeCompanyDetailsUK.dataRetrievalAction

        val result = controller(retrievalAction).onSubmit(NormalMode)(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows))
        testRenderedView(sections, result, isComplete = false)
      }

      "redirect to Session expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }
  }
}
