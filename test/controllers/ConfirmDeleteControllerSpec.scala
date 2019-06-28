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
import identifiers.register.partnership.partners.PartnerDetailsId
import models._
import models.requests.DataRequest
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

class ConfirmDeleteControllerSpec extends ControllerSpecBase with MockitoSugar {

  val testIdentifier = new TypedIdentifier[PersonDetails] {
    override def toString: String = "test"
  }

  val testChange1FlagIdentifier = new TypedIdentifier[Boolean] {
    override def toString: String = "test1"
  }

  val testChange2FlagIdentifier = new TypedIdentifier[Boolean] {
    override def toString: String = "test2"
  }

  val person = PersonDetails("First", None, "Last", LocalDate.now())

  val requestDirectors: DataRequest[AnyContent] = DataRequest(
    FakeRequest().withFormUrlEncodedBody(
      "value" -> "true"
    ), "cacheId", PSAUser(UserType.Individual, None, false, None),
    UserAnswers(Json.obj("directors" -> Json.arr(Json.obj(DirectorDetailsId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )

  val requestPartners: DataRequest[AnyContent] = DataRequest(
    FakeRequest().withFormUrlEncodedBody(
      "value" -> "true"
    ), "cacheId", PSAUser(UserType.Individual, None, false, None),
    UserAnswers(Json.obj("partners" -> Json.arr(Json.obj(PartnerDetailsId.toString -> person)),
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
        id match {
          case MoreThanTenDirectorsId => Some(testChange1FlagIdentifier)
          case _ => None
        }
      }

      override def findChangeIdIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
        Some(testChange2FlagIdentifier)
      }

      override val form = formProvider()
    }

  private def viewAsString() = confirmDelete(frontendAppConfig, formProvider(), viewModel, NormalMode)(fakeRequest, messages).toString

  private def controllerWithPost(descr:String, request: DataRequest[AnyContent], id:TypedIdentifier[PersonDetails]): Unit = {
    s"redirect to already deleted view for a GET if the $descr was already deleted" in {

      val result = controller().get(viewModel, true, FakeNavigator.desiredRoute, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)

    }

    s"redirect to ${descr}s list on removal of $descr" in {

      val result = controller().post(viewModel, id, FakeNavigator.desiredRoute, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
    }

    s"set the isDelete flag to true for the selected $descr on submission of POST request" in {

      val result = controller().post(viewModel, id, FakeNavigator.desiredRoute, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(id, person.copy(isDeleted = true))
    }

    s"set the morethanten change flag to true where the morethanten flag was already true and a $descr is deleted" in {
      val result = controller().post(viewModel, id, FakeNavigator.desiredRoute, UpdateMode)(request)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(testChange1FlagIdentifier, value = true)
      FakeUserAnswersCacheConnector.verify(testChange2FlagIdentifier, value = true)
    }
  }

  "ConfirmDelete Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller().get(viewModel, false, FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    behave like controllerWithPost("director", requestDirectors, DirectorDetailsId(0))

    behave like controllerWithPost("partner", requestPartners, PartnerDetailsId(0))


  }

}
