/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.register.{BusinessNameId, BusinessTypeId}
import models._
import models.register.BusinessType.BusinessPartnership
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.UserAnswers
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(true)
  }

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

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

    status(result) mustBe OK

    contentAsString(result) mustBe
      view(
        answerSections = sections,
        postUrl = call,
        psaNameOpt = None,
        mode = NormalMode,
        isComplete = isComplete
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

  private val answerRowsNonUK = Seq(
    answerRow(
      label = Message("businessName.heading", defaultPartnership),
      answer = Seq(defaultPartnership)
    )
  )

  "CheckYourAnswers Controller" when {

    "on GET" must {

//      "render the view correctly for all the rows of answer section if business name and utr is present for UK" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsUKV2.dataRetrievalAction
//        val result = controller(retrievalAction).onPageLoad()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRows))
//        testRenderedView(sections, result)
//      }

//      "render the view correctly for all the rows of answer section if business name and address is present for NON UK" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsNonUK.dataRetrievalAction
//        val result = controller(retrievalAction).onPageLoad()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRowsNonUK))
//        testRenderedView(sections, result)
//      }
//
//      "redirect to register as business page when business name, registration info and utr is not present for UK" in {
//        val result = controller(UserAnswers().areYouInUk(answer = true).dataRetrievalAction).onPageLoad()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
//      }
//
//      "redirect to register as business page when business name, registration info and address is not present for NON UK" in {
//        val result = controller(UserAnswers().areYouInUk(answer = false).dataRetrievalAction).onPageLoad()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
//      }
//
//      "redirect to session expired page if no existing data" in {
//        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
//    }
//
//    "on POST" must {
//      "redirect to the next page when data is complete" in {
//        val retrievalAction = UserAnswers().completePartnershipDetailsUK.dataRetrievalAction
//        val result = controller(retrievalAction).onSubmit()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(onwardRoute.url)
//      }
//
//      "load the same cya page when data is not complete" in {
//        when(mockDataCompletion.isPartnershipDetailsComplete(any())).thenReturn(false)
//        val retrievalAction = UserAnswers().completePartnershipDetailsUK.dataRetrievalAction
//        val result = controller(retrievalAction).onSubmit()(fakeRequest)
//
//        val sections = Seq(AnswerSection(None, answerRows))
//        testRenderedView(sections, result, isComplete = false)
//      }
//
//      "redirect to Session expired if there is no cached data" in {
//        val result = controller(dontGetAnyData).onSubmit()(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
    }
  }
}
