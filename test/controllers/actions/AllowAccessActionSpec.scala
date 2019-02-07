package controllers.actions

import java.lang.ProcessBuilder.Redirect

import base.SpecBase
import models._
import models.requests.AuthenticatedRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Seconds
import play.api.mvc.{Controller, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import play.api.test.Helpers._

import scala.concurrent.duration
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AllowAccessActionSpec extends SpecBase  with ScalaFutures{

  class TestAllowAccessAction(mode: Mode) extends AllowAccessAction(mode) {
    override def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = super.filter(request)
  }


  "AllowAccessAction" must{

    "allow access to pages for user with no enrolment and Normal mode" in {

      val action = new TestAllowAccessAction(NormalMode)

      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None)))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(OK)
      }


    }

    "allow access to pages for user with no enrolment and Check mode" in {

      val action = new TestAllowAccessAction(CheckMode)

      val futureResult = action.filter(AuthenticatedRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, false, None)))

      whenReady(futureResult) { result =>

        result.map { _.header.status  } mustBe Some(OK)
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

        result.map { _.header.status  } mustBe Some(OK)
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
