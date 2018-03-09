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

package controllers.register.company.directors

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions._
import models.Index
import play.api.test.Helpers._
import utils.{CheckYourAnswersFactory, CountryOptions, DateHelper, InputOption}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val index = Index(0)
  val companyName = "Test Company Name"
  val directorName = "test first name test middle name test last name"
  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  val answersDD: Seq[AnswerRow] = Seq(
    AnswerRow(
      "cya.label.name",
      Seq("test first name test last name"),
      false,
      "/pension-administrator/register/company/directors/1/changeDirectorDetails"
    ),
    AnswerRow(
      "cya.label.dob",
      Seq(DateHelper.formatDate(LocalDate.now)),
      false,
      "/pension-administrator/register/company/directors/1/changeDirectorDetails")
  )

  def call = controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit()

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      checkYourAnswersFactory
    )

  def viewAsString() = check_your_answers(
    frontendAppConfig,
    Seq(
      AnswerSection(Some("directorCheckYourAnswers.directorDetails.heading"),answersDD),
      AnswerSection(Some("directorCheckYourAnswers.contactDetails.heading"),Seq.empty)
    ),
    Some(directorName),
    call
  )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired page" when {
      "director name is not present" in {
        val result = controller(getEmptyData).onPageLoad(index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
