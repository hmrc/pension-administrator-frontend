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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.BusinessDetailsControllerBehaviour.{testFixture, testRequest}
import forms.BusinessDetailsFormModel
import identifiers.TypedIdentifier
import models.BusinessDetails
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.Helpers.{status, _}
import utils.Navigator
import viewmodels.{BusinessDetailsViewModel, Message}

class BusinessDetailsControllerSpec extends ControllerSpecBase with BusinessDetailsControllerBehaviour {

  import BusinessDetailsControllerSpec._

  "BusinessDetailsController" must {
    behave like businessDetailsController(testFormModel, testViewModel, testId, createController(this))

    "strip out disallowed characters from companyName and allow submit" in {
      val invalidBusinessDetails = BusinessDetails(companyNameWithInvalidCharacters, Some("1234567890"))
      val fixture = testFixture(createController(this), testFormModel, testViewModel)
      val request = testRequest(businessDetails = Some(invalidBusinessDetails))

      val result = fixture.controller.post(testId)(request)

      status(result) mustBe SEE_OTHER
    }
  }

}

// scalastyle:off magic.number

object BusinessDetailsControllerSpec {
  private val companyNameWithInvalidCharacters = """abcdefgh~|ijklmnopqrstu!vw"xyz£01$%2^3()+-456@:;7#,.89 '&\/"""

  val testId: TypedIdentifier[BusinessDetails] = new TypedIdentifier[BusinessDetails] {}

  val testFormModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "businessDetails.error.companyName.required",
      companyNameLengthMsg = "businessDetails.error.companyName.length",
      companyNameInvalidMsg = "businessDetails.error.companyName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = Some("businessDetails.error.utr.required"),
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )

  val testViewModel: BusinessDetailsViewModel =
    BusinessDetailsViewModel(
      postCall = Call("GET", "/"),
      title = Message("businessDetails.title"),
      heading = Message("businessDetails.heading"),
      companyNameLabel = Message("businessDetails.companyName"),
      companyNameHint = Message("businessDetails.companyName.hint"),
      utrLabel = Message("businessDetails.utr"),
      utrHint = Message("businessDetails.utr.hint")
    )

  def createController(base: SpecBase): (UserAnswersCacheConnector, Navigator) => BusinessDetailsController = {
    (connector, nav) =>
      new BusinessDetailsController {
        override protected val appConfig: FrontendAppConfig = base.frontendAppConfig
        override protected val dataCacheConnector: UserAnswersCacheConnector = connector
        override protected val navigator: Navigator = nav
        override protected val formModel: BusinessDetailsFormModel = testFormModel
        override protected val viewModel: BusinessDetailsViewModel = testViewModel

        override def messagesApi: MessagesApi = base.messagesApi
      }
  }

}
