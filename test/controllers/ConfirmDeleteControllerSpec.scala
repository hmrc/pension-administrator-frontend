/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.ConfirmDeleteFormProvider
import identifiers.TypedIdentifier
import identifiers.register.company.MoreThanTenDirectorsId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership.partners.PartnerNameId
import models._
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

import scala.concurrent.ExecutionContext

class ConfirmDeleteControllerSpec extends ControllerSpecBase {

  override def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "/")

  val testIdentifier: TypedIdentifier[PersonName] = new TypedIdentifier[PersonName] {
    override def toString: String = "test"
  }

  val testChange1FlagIdentifier: TypedIdentifier[Boolean] = new TypedIdentifier[Boolean] {
    override def toString: String = "test1"
  }

  val testChange2FlagIdentifier: TypedIdentifier[Boolean] = new TypedIdentifier[Boolean] {
    override def toString: String = "test2"
  }

  private val person = PersonName("First", "Last")

  val requestDirectors: DataRequest[AnyContent] = DataRequest(
    FakeRequest().withFormUrlEncodedBody(
      "value" -> "true"
    ), "cacheId", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, ""),
    UserAnswers(Json.obj("directors" -> Json.arr(Json.obj(DirectorNameId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )

  val requestDirectorsNoValue: DataRequest[AnyContent] = DataRequest(
    FakeRequest(), "cacheId", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, ""),
    UserAnswers(Json.obj("directors" -> Json.arr(Json.obj(DirectorNameId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )

  val requestPartners: DataRequest[AnyContent] = DataRequest(
    FakeRequest().withFormUrlEncodedBody(
      "value" -> "true"
    ), "cacheId", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, ""),
    UserAnswers(Json.obj("partners" -> Json.arr(Json.obj(PartnerNameId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )

  val requestPartnersNoValue: DataRequest[AnyContent] = DataRequest(
    FakeRequest(), "cacheId", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, ""),
    UserAnswers(Json.obj("partners" -> Json.arr(Json.obj(PartnerNameId.toString -> person)),
      MoreThanTenDirectorsId.toString -> true))
  )

  private val name = "name"

  private val viewModel = ConfirmDeleteViewModel(
    FakeNavigator.desiredRoute,
    FakeNavigator.desiredRoute,
    "title", "heading", name
  )

  val formProvider = new ConfirmDeleteFormProvider()

  private def controller(): ConfirmDeleteController =
    new ConfirmDeleteController {
      override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

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

      override def form(name: String)(implicit messages: Messages): Form[Boolean] = formProvider(name)

      override val view: confirmDelete = confirmDeleteView

      override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents

      implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    }

  private val confirmDeleteView = app.injector.instanceOf[confirmDelete]

  private def viewAsString() = confirmDeleteView(formProvider(name), viewModel, NormalMode)(fakeRequest, messagesApi.preferred(fakeRequest)).toString

  "ConfirmDelete Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller().get(viewModel, isDeleted = false, FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the director was already deleted" in {

      val result = controller().get(viewModel, isDeleted = true, FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)

    }

    "redirect to directors list on removal of director" in {

      val result = controller().post(viewModel, DirectorNameId(0), FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
    }

    "set the isDelete flag to true for the selected director on submission of POST request" in {

      val result = controller().post(viewModel, DirectorNameId(0), FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(DirectorNameId(0), person.copy(isDeleted = true))
    }

    "bad request for the selected director on submission of POST request where invalid submission (no value)" in {

      val result = controller().post(viewModel, DirectorNameId(0), FakeNavigator.desiredRoute, NormalMode)(requestDirectorsNoValue)

      status(result) mustBe BAD_REQUEST
    }

    "set the morethanten change flag to true where the morethanten flag was already true and a director is deleted" in {
      val result = controller().post(viewModel, DirectorNameId(0), FakeNavigator.desiredRoute, UpdateMode)(requestDirectors)
      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(testChange1FlagIdentifier, value = true)
      FakeUserAnswersCacheConnector.verify(testChange2FlagIdentifier, value = true)
    }
  }

}
