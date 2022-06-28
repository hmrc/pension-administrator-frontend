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

package controllers.register.administratorPartnership.contactDetails

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.administratorPartnership.contactDetails.routes._
import identifiers.register.{BusinessNameId, BusinessTypeId}
import models.NormalMode
import models.register.BusinessType.BusinessPartnership
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.register.administratorPartnership.contactDetails.whatYouWillNeed

class WhatYouWillNeedControllerSpec extends ControllerSpecBase {

  private def onwardRoute = PartnershipSameContactAddressController.onPageLoad(NormalMode)

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  val data = Json.obj(
    BusinessTypeId.toString -> BusinessPartnership.toString,
    BusinessNameId.toString -> "Partnership",
  )
  val dataRetrieval = new FakeDataRetrievalAction(Some(data))

  private def controller(dataRetrievalAction: DataRetrievalAction = dataRetrieval) =
    new WhatYouWillNeedController(
      controllerComponents,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      view
    )

  private def viewAsString() = view("Partnership")(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to next page on submit" in {

      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

  }

}
