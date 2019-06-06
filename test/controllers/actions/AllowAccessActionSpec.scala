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

package controllers.actions

import base.SpecBase
import models._
import models.requests.AuthenticatedRequest
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessActionSpec extends SpecBase  with ScalaFutures{

  class TestAllowAccessAction(mode: Mode, isSuspended: Boolean = false) extends AllowAccessAction(mode, FakeMinimalPsaConnector(isSuspended)) {
    override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = super.filter(request)
  }


  "AllowAccessAction" must{

    "allow access to pages for user with no enrolment and Normal mode" in {
      val action = new TestAllowAccessAction(NormalMode)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None)))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "allow access to pages for user with no enrolment and Check mode" in {
      val action = new TestAllowAccessAction(CheckMode)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None)))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "allow access to pages for user with enrolment and Normal mode and trying to get pagesAfterEnrolment" in {
      val action = new TestAllowAccessAction(NormalMode)
      val fakeRequest = FakeRequest("GET", "controllers.register.routes.ConfirmationController.onPageLoad().url")
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "redirect to SessionExpiredPage for user with no enrolment and UpdateMode" in {
      val action = new TestAllowAccessAction(UpdateMode)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None)))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "allow access to pages for user with enrolment and UpdateMode" in {
      val action = new TestAllowAccessAction(UpdateMode)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "redirect to intercept pages for suspended user with enrolment and UpdateMode" in {
      val action = new TestAllowAccessAction(UpdateMode, isSuspended = true)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.CannotMakeChangesController.onPageLoad().url)
      }
    }

    "redirect to intercept page for user with enrolment and Normal/Check mode" in {
      val action = new TestAllowAccessAction(NormalMode)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.InterceptPSAController.onPageLoad().url)
      }
    }
  }
}
