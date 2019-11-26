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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import models.{NormalMode, UserType}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.register.psaVarianceSuccess

class PSAVarianceSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {


  "NoLongerFitAndProperController" must {

    "return OK and the correct view for a GET" in {

      val app = application(getIndividual)

      val view: psaVarianceSuccess = app.injector.instanceOf[psaVarianceSuccess]

      val request = FakeRequest(GET, controllers.register.routes.PSAVarianceSuccessController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustBe OK

      contentAsString(result) mustBe view(Some("blah"))(request, messages).toString()
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val app = application(dontGetAnyData)

      val request = FakeRequest(GET, controllers.register.routes.PSAVarianceSuccessController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

//  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
//    new PSAVarianceSuccessController(
//      frontendAppConfig,
//      FakeAuthAction(UserType.Individual),
//      FakeAllowAccessProvider(),
//      dataRetrievalAction,
//      new DataRequiredActionImpl,
//      fakeUserAnswersCacheConnector,
//      stubMessagesControllerComponents(),
//      view
//    )

  def application(data: DataRetrievalAction): Application =
    applicationBuilder(data).overrides(
      bind[AuthAction].toInstance(FakeAuthAction(UserType.Individual)),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(NormalMode)),
      bind[DataRequiredAction].to(new DataRequiredActionImpl)
    ).build()
}