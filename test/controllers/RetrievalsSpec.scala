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

package controllers

import java.time.LocalDate

import identifiers.TypedIdentifier
import models.{PSAUser, UserType}
import models.requests.DataRequest
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers

import scala.concurrent.Future

class RetrievalsSpec extends ControllerSpecBase with FrontendController with Retrievals with EitherValues with ScalaFutures {

  def dataRequest(data: JsValue): DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
    PSAUser(UserType.Organisation, None, false, None), UserAnswers(data))

  class TestController extends FrontendController with Retrievals

  val controller = new TestController()

  val success: (String) => Future[Result] = { _: String =>
    Future.successful(Ok("Success"))
  }

  val testIdentifier = new TypedIdentifier[String]{
    override def toString: String = "test"
  }

  val secondIdentifier = new TypedIdentifier[String]{
    override def toString: String = "second"
  }

  "retrieveDirectorName" must {
    "reach the intended result when companyName is found" in {

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

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrieveDirectorName(0)(success)

      status(result) must be(OK)
    }
  }

  "retrieve" must {

    "reach the intended result when identifier gets value from answers" in {

      implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test" -> "result"))

      testIdentifier.retrieve.right.value mustEqual "result"
    }

    "reach the intended result when identifier uses and to get the value from answers" in {

      implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test" -> "result", "second" -> "answer"))

      (testIdentifier and secondIdentifier).retrieve.right.value mustEqual new ~("result", "answer")
    }

    "redirect to the session expired page when cant find identifier" in {

      implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj("test1" -> "result"))

      whenReady(testIdentifier.retrieve.left.value) {
        _ mustEqual Redirect(routes.SessionExpiredController.onPageLoad())
      }
    }

    "redirect to Session Expired page when company name is not present" in {

      implicit val request: DataRequest[AnyContent] = dataRequest(Json.obj())

      val result = controller.retrieve(testIdentifier)(success)

      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)

    }

  }

}
