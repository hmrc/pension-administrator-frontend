/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.register.adviser

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, AdviserNameId}
import models.register.adviser.AdviserDetails
import models.{Address, CheckMode, NormalMode}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Link}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def postCall = controllers.register.adviser.routes.CheckYourAnswersController.onSubmit(NormalMode)

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)
  val adviserName = "test adviser name"
  val advDetails = AdviserDetails("test@test.com", "01234567890")

  val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  val validData: JsObject = Json.obj(
    AdviserNameId.toString -> adviserName,
    AdviserDetailsId.toString -> advDetails,
    AdviserAddressId.toString -> address
  )

  def adviserAddress = Seq(AnswerRow(
    "cya.label.address",
    Seq(
      address.addressLine1,
      address.addressLine2,
      address.postcode.value,
      address.country
    ),
    false,
    Link(controllers.register.adviser.routes.AdviserAddressController.onPageLoad(CheckMode).url)
  ))

  def adviserDetails = Seq(
    AnswerRow(
      "adviserName.checkYourAnswersLabel",
      Seq(adviserName),
      false,
      Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(CheckMode).url)
    ),
    AnswerRow(
      "contactDetails.email.checkYourAnswersLabel",
      Seq(advDetails.email),
      false,
      Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url)
    ),
    AnswerRow(
      "contactDetails.phone.checkYourAnswersLabel",
      Seq(advDetails.phone),
      false,
      Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url)
    )
  )

  def sections = Seq(AnswerSection(None, adviserDetails ++ adviserAddress))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      countryOptions
    )

  def viewAsString(): String =
    check_your_answers(
      frontendAppConfig,
      sections,
      postCall,
      None,
      NormalMode
    )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if there is no cached data" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session expired on a POST request if there is no cached data" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
