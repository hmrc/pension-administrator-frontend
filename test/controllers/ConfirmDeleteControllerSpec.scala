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

package controllers

import java.time.LocalDate

import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.ConfirmDeleteFormProvider
import identifiers.TypedIdentifier
import identifiers.register.company.MoreThanTenDirectorsId
import identifiers.register.company.directors.DirectorDetailsId
import models._
import models.requests.DataRequest
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

import scala.concurrent.Future

class ConfirmDeleteControllerSpec extends ControllerSpecBase with MockitoSugar {

  val testIdentifier = new TypedIdentifier[PersonDetails] {
    override def toString: String = "test"
  }

  val testChangeFlagIdentifier = new TypedIdentifier[Boolean] {
    override def toString: String = "test2"
  }

  val person = PersonDetails("First", None, "Last", LocalDate.now())

  implicit val request: DataRequest[AnyContent] = DataRequest(
    FakeRequest().withFormUrlEncodedBody(
      "value" -> "true"
    ), "cacheId", PSAUser(UserType.Individual, None, false, None),
    UserAnswers(Json.obj("directors" -> Json.arr(Json.obj(DirectorDetailsId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )



  val viewModel = ConfirmDeleteViewModel(
    FakeNavigator.desiredRoute,
    FakeNavigator.desiredRoute,
    "title", "heading"
  )

  val formProvider = new ConfirmDeleteFormProvider()

  private def controller() =
    new ConfirmDeleteController {
      override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

      override protected def appConfig: FrontendAppConfig = frontendAppConfig

      override def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

      override def findChangeIdNonIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
        Some(testChangeFlagIdentifier)
      }

      override val form = formProvider()
    }

  private def viewAsString() = confirmDelete(frontendAppConfig, formProvider(), viewModel)(fakeRequest, messages).toString

  "ConfirmDeleteDirector Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller().get(viewModel, false, FakeNavigator.desiredRoute)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the director was already deleted" in {

      val result = controller().get(viewModel, true, FakeNavigator.desiredRoute)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)

    }

    "redirect to directors list on removal of director" in {

      val result = controller().post(viewModel, DirectorDetailsId(0), FakeNavigator.desiredRoute, NormalMode)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
    }

    "set the isDelete flag to true for the selected director on submission of POST request" in {

      val result = controller().post(viewModel, DirectorDetailsId(0), FakeNavigator.desiredRoute, NormalMode)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(DirectorDetailsId(0), person.copy(isDeleted = true))
    }

    "set the morethanten change flag to true where the morethanten flag was already true and a director is deleted" in {

      val result = controller().post(viewModel, DirectorDetailsId(0), FakeNavigator.desiredRoute, NormalMode)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(testChangeFlagIdentifier, value = true)

    }

  }

}
