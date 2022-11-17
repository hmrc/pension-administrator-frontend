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

package utils.checkyouranswers

import base.SpecBase
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.checkyouranswers.Ops._
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

import java.time.LocalDate

class CheckYourAnswersSpec extends SpecBase {

  val onwardUrl = "onwardUrl"

  def testIdentifier[A]: TypedIdentifier[A] = new TypedIdentifier[A] {
    override def toString = "testId"
  }

  def dataRequest(answers: UserAnswers): DataRequest[AnyContent] =
    DataRequest(FakeRequest(), "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

  "CheckYourAnswers" when {
    "reference value " must {
      "produce row of answers for reference value" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[ReferenceValue])(ReferenceValue(value = "test-ref")).asOpt.value)

        testIdentifier[ReferenceValue].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("testId.heading"), answer = Seq("test-ref"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }
    }

    "person name" must {
      "produce row of answers for personName" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[PersonName])(PersonName("first", "last")).asOpt.value)

        testIdentifier[PersonName].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("testId.heading"), answer = Seq("first last"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }
    }

    "LocalDate" must {

      "produce row of answers for date" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[LocalDate])(LocalDate.now()).asOpt.value)

        testIdentifier[LocalDate].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("testId.heading"), answer = Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }
    }

    "TolerantIndividualCYA" must {

      "produce row with value" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[TolerantIndividual])(TolerantIndividual(Some("first"), None, Some("last"))).asOpt.value)

        TolerantIndividualCYA(Some("test.label"), None)().
          row(testIdentifier[TolerantIndividual])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("first last"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }

      "produce row with add link if it is mandatory" in {
        val request = dataRequest(UserAnswers())

        TolerantIndividualCYA(Some("test.label"), None)().
          row(testIdentifier[TolerantIndividual])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")))))
      }

      "produce no row if it is not mandatory" in {
        val request = dataRequest(UserAnswers())

        TolerantIndividualCYA(Some("test.label"), None, isMandatory = false)().
          row(testIdentifier[TolerantIndividual])(Some(Link(onwardUrl)), request.userAnswers) must equal(Nil)
      }
    }

    "DateCYA" must {

      "produce row with value" in {
        val date = LocalDate.now().minusYears(20)
        val request = dataRequest(UserAnswers().set(testIdentifier[LocalDate])(date).asOpt.value)

        DateCYA(Some("test.label"), None)().
          row(testIdentifier[LocalDate])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq(DateHelper.formatDate(date)), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }

      "produce row with add link if it is mandatory" in {
        val request = dataRequest(UserAnswers())

        DateCYA(Some("test.label"), None)().
          row(testIdentifier[LocalDate])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")))))
      }

      "produce no row if it is not mandatory" in {
        val request = dataRequest(UserAnswers())

        DateCYA(Some("test.label"), None, isMandatory = false)().
          row(testIdentifier[LocalDate])(Some(Link(onwardUrl)), request.userAnswers) must equal(Nil)
      }
    }

    "ReferenceValueCYA" must {

      "produce row with value" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[ReferenceValue])(ReferenceValue("value")).asOpt.value)

        ReferenceValueCYA(Some("test.label"), None)().
          row(testIdentifier[ReferenceValue])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("value"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }

      "produce row with add link if it is mandatory" in {
        val request = dataRequest(UserAnswers())

        ReferenceValueCYA(Some("test.label"), None)().
          row(testIdentifier[ReferenceValue])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")))))
      }

      "produce no row if it is not mandatory" in {
        val request = dataRequest(UserAnswers())

        ReferenceValueCYA(Some("test.label"), None, isMandatory = false)().
          row(testIdentifier[ReferenceValue])(Some(Link(onwardUrl)), request.userAnswers) must equal(Nil)
      }
    }

    "BooleanCYA" must {

      "produce row with value" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[Boolean])(true).asOpt.value)

        BooleanCYA(Some("test.label"), None)().
          row(testIdentifier[Boolean])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.yes"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl)))))
      }

      "produce row with add link if it is mandatory" in {
        val request = dataRequest(UserAnswers())

        BooleanCYA(Some("test.label"), None)().
          row(testIdentifier[Boolean])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")))))
      }

      "produce no row if it is not mandatory" in {
        val request = dataRequest(UserAnswers())

        BooleanCYA(Some("test.label"), None, isMandatory = false)().
          row(testIdentifier[Boolean])(Some(Link(onwardUrl)), request.userAnswers) must equal(Nil)
      }
    }

    "StringCYA" must {

      "produce row with value" in {
        val request = dataRequest(UserAnswers().set(testIdentifier[String])("value").asOpt.value)

        StringCYA(Some("test.label"), None)().
          row(testIdentifier[String])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("value"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)))))
      }

      "produce row with add link if it is mandatory" in {
        val request = dataRequest(UserAnswers())

        StringCYA(Some("test.label"), None)().
          row(testIdentifier[String])(Some(Link(onwardUrl)), request.userAnswers) must equal(Seq(
          AnswerRow(label = Message("test.label"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")))))
      }

      "produce no row if it is not mandatory" in {
        val request = dataRequest(UserAnswers())

        StringCYA(Some("test.label"), None, isMandatory = false)().
          row(testIdentifier[String])(Some(Link(onwardUrl)), request.userAnswers) must equal(Nil)
      }
    }
  }
}
