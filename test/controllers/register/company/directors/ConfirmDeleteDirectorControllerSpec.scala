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

import base.SpecBase
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.ConfirmDeleteFormProvider
import identifiers.register.company.directors.DirectorNameId
import models.{Index, NormalMode, PersonName}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteDirectorControllerSpec._

  "render the view correctly on a GET request" in {
    val request = FakeRequest(routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, firstIndex))
    val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messages).toString()
  }

  "redirect to the next page on a POST request" in {
    val request = FakeRequest(routes.ConfirmDeleteDirectorController.onSubmit(NormalMode, firstIndex)).withFormUrlEncodedBody(
        "value" -> "true")
    val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url)
  }

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()
}

object ConfirmDeleteDirectorControllerSpec extends SpecBase {
  val firstIndex = Index(0)
  val person = PersonName("First", "Last")

  private val formProvider = new ConfirmDeleteFormProvider()
  private val form = formProvider()
  val view: confirmDelete = app.injector.instanceOf[confirmDelete]
  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorNameId.toString -> person)
    ))))

  def viewModel = ConfirmDeleteViewModel(
    routes.ConfirmDeleteDirectorController.onSubmit(NormalMode, firstIndex),
    controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode),
    Message("confirmDeleteDirector.title"),
    "confirmDeleteDirector.heading",
    Some(person.fullName),
    None
  )
}
