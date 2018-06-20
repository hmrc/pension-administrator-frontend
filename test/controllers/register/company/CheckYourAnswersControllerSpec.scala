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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import models.NormalMode
import models.register.company.BusinessDetails
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{CheckYourAnswersFactory, FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      checkYourAnswersFactory
    )

  private def call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  private def viewAsString(answers: Seq[AnswerSection]) = check_your_answers(
    frontendAppConfig,
    answers,
    Some(messages("site.secondaryHeader")),
    call
  )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {

      val companyName = "companyName"
      val utr = "test-utr"

      val companyDetailsJson = Json.obj("businessDetails" -> BusinessDetails(companyName, utr))

      val companyDetailsSection = AnswerSection(
        Some("company.checkYourAnswers.company.details.heading"),
        Seq(
          AnswerRow(
            "businessDetails.companyName",
            Seq(companyName),
            answerIsMessageKey = false,
            None
          ),
          AnswerRow(
            "companyUniqueTaxReference.checkYourAnswersLabel",
            Seq(utr),
            answerIsMessageKey = false,
            None
          )
        )
      )

      val companyContactDetails = AnswerSection(
        Some("company.checkYourAnswers.company.contact.details.heading"),
        Seq.empty
      )

      val contactDetails = AnswerSection(
        Some("company.checkYourAnswers.contact.details.heading"),
        Seq.empty
      )

      val result = controller(new FakeDataRetrievalAction(Some(companyDetailsJson))).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(Seq(companyDetailsSection, companyContactDetails, contactDetails))
    }

    "redirect to Session Expired page" when {
      "no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

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
