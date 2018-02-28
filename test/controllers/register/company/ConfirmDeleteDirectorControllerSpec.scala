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
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import repositories.{FakeSessionRepository, ReactiveMongoRepository}
import views.html.register.company.confirmDeleteDirector

import scala.concurrent.Future

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase with MockitoSugar{

  val firstIndex = Index(0)

  val directorOne = DirectorDetails("John", None, "Doe", LocalDate.now())

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmDeleteDirectorController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeDataCacheConnector
    )

  def viewAsString() = confirmDeleteDirector(frontendAppConfig, firstIndex, "John Doe")(fakeRequest, messages).toString

  "ConfirmDeleteDirector Controller" must {

    "return OK and the correct view for a GET" in {

      val validData = Json.obj(
        "directors" -> Json.arr(
          Json.obj(
            "directorDetails" -> Json.obj(
              "firstName" -> "John",
              "lastName" -> "Doe",
              "dateOfBirth" -> Json.toJson(LocalDate.now())
            )
          )
        )
      )

      val data = new FakeDataRetrievalAction(Some(validData))

      val result = controller(data).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
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
      redirectLocation(result) mustBe Some(routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url)

    }
  }
}
