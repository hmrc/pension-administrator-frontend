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

package controllers.register.administratorPartnership.partnershipDetails

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.administratorPartnership.partnershipDetails.routes._
import models._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import utils.UserAnswers
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers
import org.mockito.Mockito.when
import utils.UserAnswerOps

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(true)
  }

  private def onwardRoute: Call = controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad()

  private val defaultPartnership = "limited partnership"
  private val vat = "test-vat"
  private val paye = "test-paye"
  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]
  private val mockDataCompletion = mock[DataCompletion]

  def controller(dataRetrievalAction: DataRetrievalAction) =
    new CheckYourAnswersController(
      controllerComponents,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      view
    )

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
      label = Message("hasPAYE.heading", defaultPartnership),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasPartnershipPAYEController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("enterPAYE.heading", defaultPartnership),
      answer = Seq(paye),
      changeUrl = Some(Link(PartnershipEnterPAYEController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("hasVAT.heading", defaultPartnership),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasPartnershipVATController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("hasVAT.visuallyHidden.text", defaultPartnership))
    ),
    answerRow(
      label = Message("enterVAT.heading", defaultPartnership),
      answer = Seq(vat),
      changeUrl = Some(Link(PartnershipEnterVATController.onPageLoad(CheckMode).url)),
      visuallyHiddenLabel = Some(Message("enterVAT.visuallyHidden.text", defaultPartnership))
    )
  )


  "CheckYourAnswers Controller" when {

    "on GET" must {

      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
        val retrievalAction = UserAnswers().completePartnershipDetailsUKV2.dataRetrievalAction
        val result = controller(retrievalAction).onPageLoad()(fakeRequest)

        val sections = Seq(AnswerSection(None, answerRows))
        testRenderedView(sections, result)
      }
    }

    "on POST" must {
      "redirect to the next page when save and continue is clicked" in {
        val retrievalAction = UserAnswers().completePartnershipDetailsUKV2.dataRetrievalAction
        val result = controller(retrievalAction).onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    }
  }
}
