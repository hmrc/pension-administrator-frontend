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

package controllers

import play.api.test.Helpers._
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import utils.{CheckYourAnswersFactory, CountryOptions, InputOption, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj()))

  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  def call: Call = controllers.routes.CheckYourAnswersController.onSubmit()

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      checkYourAnswersFactory
    )

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK

      val expectedViewContent = check_your_answers(frontendAppConfig, Seq(AnswerSection(None, Seq())), None, call)(fakeRequest, messages).toString

      contentAsString(result) mustBe expectedViewContent
    }

    "redirect to Session Expired for a GET if not existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
