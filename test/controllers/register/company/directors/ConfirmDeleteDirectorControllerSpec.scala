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

import java.time.LocalDate

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.company.routes.AddCompanyDirectorsController
import controllers.register.company.directors.routes.AlreadyDeletedController
import identifiers.register.company.directors.{DirectorDetailsId, DirectorId}
import models.register.company.directors.DirectorDetails
import models.{Index, NormalMode}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.register.company.directors.confirmDeleteDirector

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val firstIndex = Index(0)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteDirectorController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeDataCacheConnector
    )

  private def viewAsString() = confirmDeleteDirector(frontendAppConfig, firstIndex, "John Doe")(fakeRequest, messages).toString

  "ConfirmDeleteDirector Controller" must {

    "return OK and the correct view for a GET" in {

      val validData = Json.obj(
        "directors" -> Json.arr(
          Json.obj(
            "directorDetails" -> Json.obj(
              "firstName" -> "John",
              "lastName" -> "Doe",
              "dateOfBirth" -> Json.toJson(LocalDate.now()),
              "isDeleted" -> false
            )
          )
        )
      )

      val data = new FakeDataRetrievalAction(Some(validData))

      val result = controller(data).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the director was already deleted" in {

      val validData = Json.obj(
        "directors" -> Json.arr(
          Json.obj(
            "directorDetails" -> Json.obj(
              "firstName" -> "John",
              "lastName" -> "Doe",
              "dateOfBirth" -> Json.toJson(LocalDate.now()),
              "isDeleted" -> true
            )
          )
        )
      )

      val data = new FakeDataRetrievalAction(Some(validData))

      val result = controller(data).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(AlreadyDeletedController.onPageLoad(firstIndex).url)

    }

    "redirect to directors list on removal of director" in {

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
      redirectLocation(result) mustBe Some(AddCompanyDirectorsController.onPageLoad(NormalMode).url)

    }

    "set the isDelete flag to true for the selected director on submission of POST request" in {
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
      FakeDataCacheConnector.verify(DirectorDetailsId(firstIndex), DirectorDetails("John", None, "Doe", LocalDate.now(), true))
    }

  }

}
