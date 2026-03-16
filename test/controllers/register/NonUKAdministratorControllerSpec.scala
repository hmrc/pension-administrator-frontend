/*
 * Copyright 2026 HM Revenue & Customs
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
import controllers.actions.{FakeAllowAccessProvider, FakeAuthAction, FakeDataRetrievalAction}
import models.NormalMode
import models.admin.ukResidencyToggle
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.*
import utils.FeatureFlagMockHelper
import views.html.register.{nonUKAdministrator, nonUKCompanyPartnershipAdministrator}

class NonUKAdministratorControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with FeatureFlagMockHelper {

  val view: nonUKAdministrator = app.injector.instanceOf[nonUKAdministrator]
  val ukResidencyView: nonUKCompanyPartnershipAdministrator = app.injector.instanceOf[nonUKCompanyPartnershipAdministrator]

  override def beforeEach(): Unit = {
    featureFlagMock(ukResidencyToggle)
  }

  val dataRetrievalAction: FakeDataRetrievalAction = getEmptyData

  private def controller() =
    new NonUKAdministratorController(
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      mockFeatureFlagService,
      controllerComponents,
      view,
      ukResidencyView
    )

  private def viewAsString: String = view()(fakeRequest, messages).toString

  private def viewUkResidency: String = ukResidencyView()(fakeRequest, messages).toString

  "NonUKAdministrator Controller" must {

    "return OK and the correct view for a GET" when {
      "ukResidencyToggle is disabled" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString

      }
      "ukResidencyToggle is enabled" in {
        featureFlagMock(ukResidencyToggle, true)
        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewUkResidency
      }
    }
  }
}