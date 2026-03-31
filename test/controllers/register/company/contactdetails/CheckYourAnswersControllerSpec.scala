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

package controllers.register.company.contactdetails

import controllers.ControllerSpecBase
import controllers.actions.*
import models.*
import models.admin.ukResidencyToggle
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers.*
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeCountryOptions, FeatureFlagMockHelper, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach
    with FeatureFlagMockHelper {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isCompanyDetailsComplete(any())).thenReturn(true)
    featureFlagMock(ukResidencyToggle)
  }

  private def onwardRoute: Call = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()

  private val defaultCompany = "test company"
  private val addressYears = AddressYears.UnderAYear
  private val email = "test@test.com"
  private val phone = "111"
  private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "Country of GB")
  private val addressUK = AddressUKOnly("Telford1", "Telford2", None, None, "TF3 4ER")
  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]
  private val mockDataCompletion: DataCompletion = mock[DataCompletion]

  def controller(dataRetrievalAction: DataRetrievalAction) =
    new CheckYourAnswersController(
      controllerComponents,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockFeatureFlagService,
      view
    )(new FakeCountryOptions(environment, frontendAppConfig))

  private def call = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()

  private def answerRow(
                         label: String,
                         answer: Seq[String],
                         answerIsMessageKey: Boolean = false,
                         changeUrl: Option[Link],
                         visuallyHiddenLabel: Option[Message]
                       ): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def testRenderedView(
                                sections: Seq[AnswerSection],
                                result: Future[Result],
                                isComplete: Boolean = true
                              ): Unit = {

    status(result) mustBe OK

    contentAsString(result) mustBe
      view(
        answerSections = sections,
        postUrl = call,
        psaNameOpt = Some(defaultCompany),
        mode = NormalMode,
        isComplete = isComplete,
        businessNameId = None,
        returnLink = Some(call.url)
      )(fakeRequest, messages).toString()
  }

  private def answerRows(ukResidency: Boolean) =
    val contactAddress = if(ukResidency)  {answerRow(
      label = Message("cya.label.contact.address", defaultCompany),
      answer = Seq(
        addressUK.addressLine1,
        addressUK.addressLine2,
        addressUK.postcode,
      ),
      changeUrl = Some(Link(controllers.register.company.routes.CompanySameContactAddressController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultCompany))
    )} else {
      answerRow(
        label = Message("cya.label.contact.address", defaultCompany),
        answer = Seq(
          address.addressLine1,
          address.addressLine2,
          address.postcode.value,
          address.country
        ),
        changeUrl = Some(Link(controllers.register.company.routes.CompanySameContactAddressController.onPageLoad(CheckMode).url)),
        visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultCompany))
      )
    }
    Seq(
      contactAddress,
      answerRow(
        label = Message("addressYears.heading", defaultCompany),
        answer = Seq(s"common.addressYears.${addressYears.toString}"),
        answerIsMessageKey = true,
        changeUrl = Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
        visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultCompany))
      ),
      answerRow(
        label = Message("trading.title", defaultCompany),
        answer = Seq("site.yes"),
        answerIsMessageKey = true,
        changeUrl = Some(Link(controllers.register.company.routes.CompanyTradingOverAYearController.onPageLoad(CheckMode).url)),
        visuallyHiddenLabel = Some(Message("trading.visuallyHidden.text", defaultCompany))
      ),
      answerRow(
        label = Message("previousAddress.checkYourAnswersLabel", defaultCompany),
        answer = Seq(
          address.addressLine1,
          address.addressLine2,
          address.postcode.value,
          address.country
        ),
        changeUrl = Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)),
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

    "on GET" must {

      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
        val retrievalAction = UserAnswers().completeCompanyDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad()(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows(false)))
        testRenderedView(sections, result)
      }
      "render the view correctly for all the rows of answer section if business name and utr is present for UK when toggle enabled" in {
        featureFlagMock(ukResidencyToggle, true)
        val retrievalAction = UserAnswers().completeCompanyDetailsUKResidency.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad()(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows(true)))
        testRenderedView(sections, result)
      }
    }

    "on POST" must {
      "redirect to the next page when save and continue is clicked" in {
        val retrievalAction = UserAnswers().completeCompanyDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    }
  }
}
