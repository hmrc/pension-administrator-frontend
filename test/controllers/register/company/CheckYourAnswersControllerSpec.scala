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
import identifiers.register.company.CompanyDetailsId
import models.register.company.CompanyDetails
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{CheckYourAnswersFactory, CountryOptions, InputOption}
import viewmodels.Section
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      checkYourAnswersFactory
    )

  def call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  def viewAsString() = check_your_answers(frontendAppConfig, Seq.empty[Section], Some(messages("site.secondary")), call)(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {

      val companyDetails = Json.obj("companyDetails" -> CompanyDetails("companyName", None, None))

      val result = controller(new FakeDataRetrievalAction(Some(companyDetails))).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired page" when {
      "no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
