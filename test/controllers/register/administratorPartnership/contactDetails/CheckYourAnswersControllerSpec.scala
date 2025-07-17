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

package controllers.register.administratorPartnership.contactDetails

import controllers.ControllerSpecBase
import controllers.actions.*
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers.*
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeCountryOptions, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(true)
  }

  private def onwardRoute: Call = controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad()

  private val defaultPartnership = "limited partnership"
  private val addressYears = AddressYears.UnderAYear
  private val email = "test@test.com"
  private val phone = "111"
  private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "Country of GB")
  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]
  private val mockDataCompletion: DataCompletion = mock[DataCompletion]

  def controller(dataRetrievalAction: DataRetrievalAction) =
    new CheckYourAnswersController(
      controllerComponents,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      view
    )(new FakeCountryOptions(environment, frontendAppConfig))

  private def call = controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad()

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
        psaNameOpt = None,
        mode = NormalMode,
        isComplete = isComplete,
        businessNameId = Some(defaultPartnership)
      )(fakeRequest, messages).toString()
  }

  private val answerRows = Seq(
    answerRow(
      label = Message("cya.label.contact.address", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipSameContactAddressController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("contactAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("addressYears.heading", defaultPartnership),
      answer = Seq(s"common.addressYears.${addressYears.toString}"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("trading.title", defaultPartnership),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(routes.PartnershipTradingOverAYearController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("trading.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("previousAddress.checkYourAnswersLabel", defaultPartnership),
      answer = Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      changeUrl = Some(Link(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("email.title", defaultPartnership),
      answer = Seq(email),
      changeUrl = Some(Link(routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = messages("phone.title", defaultPartnership),
      answer = Seq(phone),
      changeUrl = Some(Link(routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
    )
  )


  "CheckYourAnswers Controller" when {

    "on GET" must {

      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
        val retrievalAction = UserAnswers().completePartnershipContactDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad()(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows))
        testRenderedView(sections, result)
      }
    }


    "on POST" must {
      "redirect to the next page when save and continue is clicked" in {
        val retrievalAction = UserAnswers().completePartnershipContactDetailsUK.dataRetrievalAction
        val result = controller(retrievalAction).onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    }
  }
}
