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

package controllers.register.company.directors

import java.time.LocalDate

import base.CSRFRequest
import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.company.directors.DirectorDetailsId
import models.{Index, NormalMode, PersonDetails}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

import scala.concurrent.Future

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase with CSRFRequest {

  import ConfirmDeleteDirectorControllerSpec._

  "render the view correctly on a GET request" in {
    requestResult(dataRetrieval)(
      implicit app => addToken(FakeRequest(routes.ConfirmDeleteDirectorController.onPageLoad(firstIndex))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe confirmDelete(frontendAppConfig, viewModel)(request, messages).toString()
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(dataRetrieval)(
      implicit app => addToken(FakeRequest(routes.ConfirmDeleteDirectorController.onSubmit(firstIndex))),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url)
      }
    )
  }

  def requestResult[T](dataRetrieval: DataRetrievalAction)
                      (request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                      (implicit w: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}

object ConfirmDeleteDirectorControllerSpec {
  val firstIndex = Index(0)
  val person = PersonDetails("First", None, "Last", LocalDate.now())

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorDetailsId.toString -> person)
    ))))

  def viewModel = ConfirmDeleteViewModel(
    routes.ConfirmDeleteDirectorController.onSubmit(firstIndex),
    controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode),
    Message("confirmDeleteDirector.title"),
    "confirmDeleteDirector.heading",
    Some(person.fullName),
    None
  )
}
