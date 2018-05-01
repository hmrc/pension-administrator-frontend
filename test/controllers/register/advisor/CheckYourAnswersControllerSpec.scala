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

package controllers.register.advisor

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.advisor.{AdvisorAddressId, AdvisorDetailsId}
import models.{Address, CheckMode, NormalMode}
import models.register.advisor.AdvisorDetails
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils._
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()
  def postCall = controllers.register.advisor.routes.CheckYourAnswersController.onSubmit()
  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)
  val advDetails = AdvisorDetails("test advisor name", "test@test.com", "01234567890")
  val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  val validData = Json.obj(
    AdvisorDetailsId.toString -> advDetails,
    AdvisorAddressId.toString -> address
  )

  def advisorAddress = Seq(AnswerRow(
    "cya.label.address",
    Seq(
      s"${address.addressLine1},",
      s"${address.addressLine2},",
      s"${address.postcode.value},",
      address.country
    ),
    false,
    controllers.register.advisor.routes.AdvisorAddressController.onPageLoad(CheckMode).url
  ))

  def advisorDetails = Seq(AnswerRow("cya.label.name", Seq(advDetails.name), false, controllers.register.advisor.routes.AdvisorDetailsController.onPageLoad(CheckMode).url),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(advDetails.email), false, controllers.register.advisor.routes.AdvisorDetailsController.onPageLoad(CheckMode).url))
  def sections = Seq(AnswerSection(None, advisorDetails ++ advisorAddress))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, checkYourAnswersFactory)

  def viewAsString() = check_your_answers(frontendAppConfig, sections, Some("common.advisor.secondary.heading"), postCall)(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if there is no cached data" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session expired on a POST request if there is no cached data" in {
      val result = controller(dontGetAnyData).onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
