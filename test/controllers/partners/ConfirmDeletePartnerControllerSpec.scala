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

package controllers.partners

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, _}
import controllers.register.partnership.partners.{ConfirmDeletePartnerController, routes}
import identifiers.register.partnership.partners.PartnerDetailsId
import models.{Index, PersonDetails}
import play.api.libs.json.Json
import play.api.test.Helpers._
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

class ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeletePartnerController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  val firstIndex = Index(0)

  val person = PersonDetails("First", None, "Last", LocalDate.now())

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "partners" -> Json.arr(
      Json.obj(PartnerDetailsId.toString -> person)
    ))))

  def viewModel = ConfirmDeleteViewModel(
    routes.ConfirmDeletePartnerController.onSubmit(firstIndex),
    routes.ConfirmDeletePartnerController.onPageLoad(firstIndex),
    Message("confirmDelete.partner.title"),
    "confirmDelete.partner.heading",
    Some(person.fullName)
  )

  def viewAsString() = confirmDelete(frontendAppConfig, firstIndex, viewModel)(fakeRequest, messages).toString

  "ConfirmDeletePartner Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller(dataRetrieval).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}
