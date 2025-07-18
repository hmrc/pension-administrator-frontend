/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models.NormalMode
import play.api.mvc.Call
import play.api.test.Helpers._
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

class AlreadyDeletedControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.register.administratorPartnership.routes.AddPartnerController.onPageLoad(NormalMode)

  private val partnerName = "test first name test last name"

  def viewModel: AlreadyDeletedViewModel = AlreadyDeletedViewModel(Message("alreadyDeleted.partner.title"), partnerName, onwardRoute)

  def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new AlreadyDeletedController(
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  val view: alreadyDeleted = app.injector.instanceOf[alreadyDeleted]

  def viewAsString(): String = view(
    viewModel
  )(fakeRequest, messages).toString

  "AlreadyDeleted Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(firstIndex, NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(firstIndex, NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }
}
