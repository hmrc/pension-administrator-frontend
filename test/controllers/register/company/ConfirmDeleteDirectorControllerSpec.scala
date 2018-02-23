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

package controllers.register.company

import java.time.LocalDate

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.company.DirectorDetailsId
import models.register.company.DirectorDetails
import models.{Index, NormalMode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.register.company.confirmDeleteDirector

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  val firstIndex = Index(0)

  val directorOne = DirectorDetails("John", None, "Doe", LocalDate.now())

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteDirectorController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)

  def viewAsString() = confirmDeleteDirector(frontendAppConfig)(fakeRequest, messages).toString

  "ConfirmDeleteDirector Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to directors list on removal director" in {

      val validData = Json.obj(
        "directors" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString -> DirectorDetails("John", None, "Doe", LocalDate.now())
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onSubmit(firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url)

      FakeDataCacheConnector.verify(DirectorDetailsId.collectionPath, Seq.empty[DirectorDetails])

    }
  }
}
