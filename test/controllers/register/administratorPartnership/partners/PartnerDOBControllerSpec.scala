/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partners

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.routes._
import models.NormalMode
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.dob

class PartnerDOBControllerSpec extends ControllerSpecBase {

  "PartnerDOBController" must {

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "2019-10-23"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
    }

  }

  def controller(dataRetrievalAction: DataRetrievalAction): PartnerDOBController =
    new PartnerDOBController(
      appConfig = frontendAppConfig,
      cacheConnector = FakeUserAnswersCacheConnector,
      navigator = FakeNavigator,
      authenticate = FakeAuthAction,
      allowAccess = FakeAllowAccessProvider(config = frontendAppConfig),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl(),
      controllerComponents = controllerComponents,
      view = app.injector.instanceOf[dob]
    )
}

