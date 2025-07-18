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

package controllers.register.adviser

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.adviser.{AdviserAddressId, AdviserEmailId, AdviserNameId, AdviserPhoneId}
import models.{Address, CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import utils._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import viewmodels.{AnswerRow, AnswerSection, Link}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  private def sections: Seq[AnswerSection] = Seq(AnswerSection(None, adviserDetails))

  private val mockDataCompletion = mock[DataCompletion]
  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =

    new CheckYourAnswersController(
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockDataCompletion,
      countryOptions,
      controllerComponents,
      view
    )

  private def viewAsString(isComplete: Boolean = true): String =
    view(
      sections,
      postCall,
      None,
      NormalMode,
      isComplete
    )(fakeRequest, messages).toString

  override def beforeEach(): Unit = {
    when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
  }

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
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "reload the cya page on a POST request when adviser is not complete" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(false)
      val result = controller(getRelevantData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(isComplete = false)
    }

    "redirect to Session expired on a POST request if there is no cached data" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

  private def onwardRoute = controllers.routes.IndexController.onPageLoad
  private def postCall = controllers.register.adviser.routes.CheckYourAnswersController.onSubmit(NormalMode)

  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val adviserName = "test adviser name"
  private val advEmail = "test@test.com"
  private val advPhone = "01234567890"

  private val address = Address("address-line-1", "address-line-2", None, None, Some("post-code"), "country")

  private val validData: JsObject = Json.obj(
    AdviserNameId.toString -> adviserName,
    AdviserEmailId.toString -> advEmail,
    AdviserPhoneId.toString -> advPhone,
    AdviserAddressId.toString -> address
  )

  private def adviserDetails: Seq[AnswerRow] = Seq(
    AnswerRow(
      messages("adviserName.heading"),
      Seq(adviserName),
      answerIsMessageKey = false,
      Some(Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(CheckMode).url)),
      Some(messages("adviserName.visuallyHidden.text"))
    ),
    AnswerRow(
      messages("addressFor.label", adviserName),
      Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      answerIsMessageKey = false,
      Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
      Some(messages("addressFor.visuallyHidden.text", adviserName))
    ),
    AnswerRow(
      messages("email.title", adviserName),
      Seq(advEmail),
      answerIsMessageKey = false,
      Some(Link(controllers.register.adviser.routes.AdviserEmailController.onPageLoad(CheckMode).url)),
      Some(messages("email.visuallyHidden.text", adviserName))
    ),
    AnswerRow(
      messages("phone.title", adviserName),
      Seq(advPhone),
      answerIsMessageKey = false,
      Some(Link(controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(CheckMode).url)),
      Some(messages("phone.visuallyHidden.text", adviserName))
    )
  )

}
