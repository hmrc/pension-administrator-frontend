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

///*
// * Copyright 2018 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
package controllers.register.partnership

import base.SpecBase
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import controllers.{BusinessDetailsControllerBehaviour, ControllerSpecBase}
import forms.BusinessDetailsFormModel
import identifiers.register.partnership.PartnershipDetailsId
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}
import viewmodels.{BusinessDetailsViewModel, Message}

class PartnerShipBusinessDetailsControllerSpec extends ControllerSpecBase
  with BusinessDetailsControllerBehaviour {

  import PartnershipCompanyBusinessDetailsControllerSpec._

  "BusinessDetails Controller" must {

    appRunning()
    behave like businessDetailsController(testFormModel, testViewModel, PartnershipDetailsId, createController(this, getEmptyData))

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

object PartnershipCompanyBusinessDetailsControllerSpec {
  val testFormModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "partnershipBusinessDetails.error.partnershipName.required",
      companyNameLengthMsg = "partnershipBusinessDetails.error.partnershipName.length",
      companyNameInvalidMsg = "partnershipBusinessDetails.error.partnershipName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = "partnershipBusinessDetails.error.utr.required",
      utrLengthMsg = "partnershipBusinessDetails.error.utr.length",
      utrInvalidMsg = "partnershipBusinessDetails.error.utr.invalid"
    )

  lazy val testViewModel: BusinessDetailsViewModel =
    BusinessDetailsViewModel(
      postCall = routes.PartnershipBusinessDetailsController.onSubmit(),
      title = Message("partnershipBusinessDetails.title"),
      heading = Message("partnershipBusinessDetails.heading"),
      companyNameLabel = Message("partnershipBusinessDetails.partnershipName"),
      companyNameHint = Message("partnershipBusinessDetails.partnershipName.hint"),
      utrLabel = Message("partnershipBusinessDetails.utr"),
      utrHint = Message("partnershipBusinessDetails.utr.hint")
    )

  def createController(base: SpecBase, dataRetrieval: DataRetrievalAction): (DataCacheConnector, Navigator) => PartnershipBusinessDetailsController = {

    (connector, nav) =>
      new PartnershipBusinessDetailsController(
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
