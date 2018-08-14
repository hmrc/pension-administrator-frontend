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

import base.SpecBase
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import controllers.{BusinessDetailsControllerBehaviour, ControllerSpecBase}
import forms.BusinessDetailsFormModel
import identifiers.register.company.BusinessDetailsId
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}
import viewmodels.{BusinessDetailsViewModel, Message}

class CompanyBusinessDetailsControllerSpec extends ControllerSpecBase
  with BusinessDetailsControllerBehaviour {

  import CompanyBusinessDetailsControllerSpec._

  "BusinessDetails Controller" must {

    appRunning()
    behave like businessDetailsController(testFormModel, testViewModel, BusinessDetailsId, createController(this, getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = createController(this, dontGetAnyData)(FakeDataCacheConnector, FakeNavigator).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody()
      val result = createController(this, dontGetAnyData)(FakeDataCacheConnector, FakeNavigator).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

// scalastyle:off magic.number

object CompanyBusinessDetailsControllerSpec {
  val testFormModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "businessDetails.error.companyName.required",
      companyNameLengthMsg = "businessDetails.error.companyName.length",
      companyNameInvalidMsg = "businessDetails.error.companyName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = "businessDetails.error.utr.required",
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )

  lazy val testViewModel: BusinessDetailsViewModel =
    BusinessDetailsViewModel(
      postCall = routes.CompanyBusinessDetailsController.onSubmit(),
      title = Message("businessDetails.title"),
      heading = Message("businessDetails.heading"),
      companyNameLabel = Message("businessDetails.companyName"),
      companyNameHint = Message("businessDetails.companyName.hint"),
      utrLabel = Message("businessDetails.utr"),
      utrHint = Message("businessDetails.utr.hint")
    )

  def createController(base: SpecBase, dataRetrieval: DataRetrievalAction): (DataCacheConnector, Navigator) => CompanyBusinessDetailsController = {

    (connector, nav) =>
      new CompanyBusinessDetailsController(
        base.frontendAppConfig,
        base.messagesApi,
        connector,
        nav,
        FakeAuthAction,
        dataRetrieval,
        new DataRequiredActionImpl
      )
  }
}
