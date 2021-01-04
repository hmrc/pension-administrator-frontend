/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.ConfirmDeleteFormProvider
import identifiers.TypedIdentifier
import identifiers.register.company.MoreThanTenDirectorsId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership.partners.PartnerNameId
import models._
import models.requests.DataRequest
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

import scala.concurrent.ExecutionContext

class ConfirmDeleteControllerSpec extends ControllerSpecBase with MockitoSugar {

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

      override def form(name: String)(implicit messages: Messages): Form[Boolean] = formProvider(name)

      override val view: confirmDelete = confirmDeleteView

      override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()

      implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    }

  private val confirmDeleteView = app.injector.instanceOf[confirmDelete]

  private def viewAsString() = confirmDeleteView(formProvider(name), viewModel, NormalMode)(fakeRequest, messagesApi.preferred(fakeRequest)).toString

  private def controllerWithPost(descr: String, request: DataRequest[AnyContent], requestNoValue: DataRequest[AnyContent], id: TypedIdentifier[PersonName]): Unit = {
    s"redirect to already deleted view for a GET if the $descr was already deleted" in {

      val result = controller().get(viewModel, isDeleted = true, FakeNavigator.desiredRoute, NormalMode)(request)

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

    s"bad request for the selected $descr on submission of POST request where invalid submission (no value)" in {

      val result = controller().post(viewModel, id, FakeNavigator.desiredRoute, NormalMode)(requestNoValue)

      status(result) mustBe BAD_REQUEST
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

      val result = controller().get(viewModel, isDeleted = false, FakeNavigator.desiredRoute, NormalMode)(requestDirectors)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    behave like controllerWithPost("director", requestDirectors, requestDirectorsNoValue, DirectorNameId(0))


  }

}
