/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.partnership.partners

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, _}
import forms.ConfirmDeleteFormProvider
import identifiers.register.partnership.partners.PartnerNameId
import models.{Index, NormalMode, PersonName}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.{Partnership, PartnershipPartner}
import utils.{FakeNavigator, Navigator}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

class ConfirmDeletePartnerControllerSpec extends ControllerSpecBase {

  override val firstIndex: Index = Index(0)

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

  val postUrl: Call = controllers.register.partnership.routes.AddPartnerController.onPageLoad(NormalMode)
  val redirectUrl: Call = routes.ConfirmDeletePartnerController.onSubmit(firstIndex, NormalMode)
  private val formProvider = new ConfirmDeleteFormProvider()
  val person: PersonName = PersonName("First", "Last")

  private val form = formProvider(person.fullName)

  val view: confirmDelete = app.injector.instanceOf[confirmDelete]

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "partners" -> Json.arr(
      Json.obj(PartnerNameId.toString -> person)
    ))))

  "render the view correctly on a GET request" in {

    val request = addCSRFToken(FakeRequest(GET, routes.ConfirmDeletePartnerController.onPageLoad(firstIndex, NormalMode).url))

    val result = route(application, request).value

    status(result) mustBe OK

    contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messagesApi.preferred(request)).toString()

    application.stop()
  }

  "redirect to the next page on a POST request" in {
    running(_.overrides(modules(dataRetrieval)++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[PartnershipPartner]).toInstance(new FakeNavigator(postUrl)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
      ):_*)) {
      app =>
        val controller = app.injector.instanceOf[ConfirmDeletePartnerController]

        val request = FakeRequest().withFormUrlEncodedBody("value" -> "true")

        val result = controller.onSubmit(0, NormalMode)(request)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(postUrl.url)

        application.stop()
    }
  }


  def viewModel = ConfirmDeleteViewModel(
    redirectUrl,
    postUrl,
    Message("confirmDelete.partner.title"),
    "confirmDelete.partner.heading",
    person.fullName,
    None
  )

}
