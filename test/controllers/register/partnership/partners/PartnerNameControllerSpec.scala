/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.partnership.partners

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.{ControllerSpecBase, PersonNameControllerBehaviour}
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._

import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.personName

class PartnerNameControllerSpec extends ControllerSpecBase with PersonNameControllerBehaviour {

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers())

  "PartnerNameController" must {

    val controller = testController(getEmptyData)
    val viewModel = controller.viewModel(NormalMode, 0, "test name")
    val id = controller.id(0)

    behave like personNameController(viewModel, id, createController(getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = testController(dontGetAnyData).onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Doe"))
      val result = testController(dontGetAnyData).onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

  def testController(dataRetrievalAction: DataRetrievalAction): PartnerNameController =
    createController(dataRetrievalAction)(FakeUserAnswersCacheConnector, FakeNavigator)

  def createController(dataRetrievalAction: DataRetrievalAction)
                      (connector: UserAnswersCacheConnector, nav: Navigator): PartnerNameController =
    new PartnerNameController(
      appConfig = frontendAppConfig,
      cacheConnector = connector,
      navigator = nav,
      authenticate = FakeAuthAction,
      allowAccess = FakeAllowAccessProvider(config = frontendAppConfig),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl(),
      controllerComponents = controllerComponents,
      view = app.injector.instanceOf[personName]
    )

}
