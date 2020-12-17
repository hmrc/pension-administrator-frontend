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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import identifiers.RLSFlagId
import org.mockito.Matchers._
import org.mockito.Mockito._
import models._
import models.requests.AuthenticatedRequest
import org.mockito.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsNull
import play.api.mvc.Call
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowAccessActionSpec extends SpecBase with ScalaFutures{

  private val minimalPsa = MinimalPSA(
    email = "a@a.c",
    isPsaSuspended = false,
    organisationName = None,
    individualDetails = None,
    rlsFlag = false
  )

  val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  class TestAllowAccessAction(mode: Mode, minimalPsa: MinimalPSA, config:FrontendAppConfig) extends
    AllowAccessAction(mode, FakeMinimalPsaConnector(minimalPsa), frontendAppConfig, mockUserAnswersCacheConnector) {
    override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = super.filter(request)
  }

  "AllowAccessAction" must {

    "allow access to pages for user with no enrolment and Normal mode" in {
      val action = new TestAllowAccessAction(NormalMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, "")))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "allow access to pages for user with no enrolment and Check mode" in {
      val action = new TestAllowAccessAction(CheckMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, "")))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "allow access to pages for user with enrolment and Normal mode and trying to get pagesAfterEnrolment" in {
      val action = new TestAllowAccessAction(NormalMode, minimalPsa, config = frontendAppConfig)
      val fakeRequest = FakeRequest("GET", "controllers.register.routes.ConfirmationController.onPageLoad().url")
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "redirect to SessionExpiredPage for user with no enrolment and UpdateMode" in {
      val action = new TestAllowAccessAction(UpdateMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, "")))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "allow access to pages for user with enrolment and UpdateMode" in {
      val action = new TestAllowAccessAction(UpdateMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe None
      }
    }

    "redirect to intercept pages for suspended user with enrolment and UpdateMode" in {
      val minimalPsa = MinimalPSA(
        email = "a@a.c",
        isPsaSuspended = true,
        organisationName = None,
        individualDetails = None,
        rlsFlag = false
      )
      val action = new TestAllowAccessAction(UpdateMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, Some("id"))))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.CannotMakeChangesController.onPageLoad().url)
      }
    }

    "redirect to intercept page for user with enrolment and Normal/Check mode" in {
      val action = new TestAllowAccessAction(NormalMode, minimalPsa, config = frontendAppConfig)
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, Some("id"))))

      whenReady(futureResult) { result =>
        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(Call("GET", frontendAppConfig.schemesOverviewUrl).url)
      }
    }

    "redirect to update contact address page for user with enrolment where RLS flag is set and update Mongo cache with RLS flag" in {
      val minimalPsa = MinimalPSA(
        email = "a@a.c",
        isPsaSuspended = false,
        organisationName = None,
        individualDetails = None,
        rlsFlag = true
      )

      when(mockUserAnswersCacheConnector.save(any(), Matchers.eq(RLSFlagId), Matchers.eq(true))(any(), any(), any()))
        .thenReturn(Future.successful(JsNull))

      val action = new TestAllowAccessAction(UpdateMode, minimalPsa, config = frontendAppConfig)
      val fakeRequest = FakeRequest("GET", "controllers.register.routes.ConfirmationController.onPageLoad().url")
      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, Some("id"))))

      whenReady(futureResult) { result =>
        result.map { _.header.status  } mustBe Some(SEE_OTHER)
        result.flatMap { _.header.headers.get(LOCATION)  } mustBe Some(controllers.routes.UpdateContactAddressController.onPageLoad().url)
      }
    }
  }
}
