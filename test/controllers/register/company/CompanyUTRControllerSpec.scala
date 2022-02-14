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

package controllers.register.company

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.register.UTRControllerBehaviour
import identifiers.register.BusinessUTRId
import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.{SEE_OTHER, redirectLocation, status, _}

import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.register.utr

class CompanyUTRControllerSpec extends ControllerSpecBase with UTRControllerBehaviour {

  override val view: utr = app.injector.instanceOf[utr]

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers())

  "CompanyUTRController" must {

    behave like utrController(BusinessUTRId, createController(this, getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = testController(this, dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Doe"))
      val result = testController(this, dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

  def testController(
                      base: ControllerSpecBase,
                      dataRetrievalAction: DataRetrievalAction
                    ): CompanyUTRController =
    createController(base, dataRetrievalAction)(FakeUserAnswersCacheConnector, FakeNavigator)

  def createController(
                        base: ControllerSpecBase,
                        dataRetrievalAction: DataRetrievalAction
                      )(connector: UserAnswersCacheConnector, nav: Navigator): CompanyUTRController =
    new CompanyUTRController(
      appConfig = base.frontendAppConfig,
      cacheConnector = connector,
      navigator = nav,
      authenticate = FakeAuthAction,
      allowAccess = FakeAllowAccessProvider(config = frontendAppConfig),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl(),
      controllerComponents, view
    )

}
