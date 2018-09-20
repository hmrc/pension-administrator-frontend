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

package controllers.register.company.directors

import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import controllers.actions._
import controllers.{ControllerSpecBase, PersonDetailsControllerBehaviour}
import models.NormalMode
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}

class DirectorDetailsControllerSpec extends ControllerSpecBase with PersonDetailsControllerBehaviour {

  import DirectorDetailsControllerSpec._

  "DirectorDetailsController" must {

    val controller = testController(this, getEmptyData)
    val viewModel = controller.viewModel(NormalMode, 0)
    val id = controller.id(0)

    behave like personDetailsController(viewModel, id, createController(this, getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = testController(this, dontGetAnyData).onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Doe"))
      val result = testController(this, dontGetAnyData).onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object DirectorDetailsControllerSpec {

  def testController(
                      base: ControllerSpecBase,
                      dataRetrievalAction: DataRetrievalAction
                    ): DirectorDetailsController =
    createController(base, dataRetrievalAction)(FakeUserAnswersCacheConnector, FakeNavigator)

  def createController(
                        base: ControllerSpecBase,
                        dataRetrievalAction: DataRetrievalAction
                      )(connector: UserAnswersCacheConnector, nav: Navigator): DirectorDetailsController =
    new DirectorDetailsController(
      appConfig = base.frontendAppConfig,
      messagesApi = base.messagesApi,
      dataCacheConnector = connector,
      navigator = nav,
      authenticate = FakeAuthAction,
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl()
    )

}
