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

package controllers.register.partnership.partners

import java.time.LocalDate

import base.CSRFRequest
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.ContactDetailsFormProvider
import identifiers.register.partnership.partners.PartnerDetailsId
import models.{Index, NormalMode, PersonDetails}
import org.scalatest.OptionValues
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.{ContactDetailsViewModel, Message}
import views.html.contactDetails

import scala.concurrent.Future

class PartnerContactDetailsControllerSpec extends ControllerSpecBase with CSRFRequest {

  import PartnerContactDetailsControllerSpec._

  "render the view correctly on a GET request" in {
    requestResult(dataRetrieval)(
      implicit app => addToken(FakeRequest(routes.PartnerContactDetailsController.onPageLoad(NormalMode, firstIndex))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe contactDetails(frontendAppConfig, formProvider(), viewModel)(request, messages).toString()
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(dataRetrieval)(
      implicit app => addToken(FakeRequest(routes.PartnerContactDetailsController.onSubmit(NormalMode, firstIndex))
        .withFormUrlEncodedBody("emailAddress" -> "e@mail.co", "phoneNumber" -> "232")),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    )
  }
}

object PartnerContactDetailsControllerSpec extends OptionValues {

  val partnershipName = "Test Partner"
  val firstIndex = Index(0)

  val onwardRoute = controllers.register.partnership.partners.routes.CheckYourAnswersController.onPageLoad(firstIndex)

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "partners" -> Json.arr(Json.obj(
      PartnerDetailsId.toString -> PersonDetails("Test", None, "Partner", LocalDate.now())
    )))))

  val formProvider = new ContactDetailsFormProvider()

  val viewModel = ContactDetailsViewModel(
    routes.PartnerContactDetailsController.onSubmit(NormalMode, firstIndex),
    Message("partnership.partner.contactDetails.title"), Message("partnership.partner.contactDetails.heading"), None, Some(partnershipName)
  )

  def requestResult[T](dataRetrieval: DataRetrievalAction)
                      (request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                      (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}
