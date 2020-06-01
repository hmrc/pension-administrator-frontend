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

import java.time.LocalDate

import com.google.inject.Inject
import identifiers.TypedIdentifier
import identifiers.register.partnership.partners.PartnerNameId
import models._
import models.requests.DataRequest
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Results._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers

import scala.concurrent.Future

class RetrievalsSpec extends ControllerSpecBase with Retrievals with EitherValues with ScalaFutures {

  def dataRequest(data: JsValue): DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers(data))

  class TestController @Inject()(
                                  val controllerComponents: MessagesControllerComponents
                                ) extends FrontendBaseController with Retrievals

  val controller = new TestController(stubMessagesControllerComponents())

  val success: String => Future[Result] = { _: String =>
    Future.successful(Ok("Success"))
  }

  val testIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
    override def toString: String = "test"
  }

  val secondIdentifier: TypedIdentifier[String] = new TypedIdentifier[String] {
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
              "dateOfBirth" -> Json.toJson(LocalDate.now()),
              "isDeleted" -> false
            )
          )
        )
      )

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrieveDirectorName(NormalMode, 0)(success)

      status(result) must be(OK)
    }
  }

  "retrievePartnerName" must {
    "reach the intended result when partner name is found" in {
      val validData = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerNameId.toString -> PersonName("john", "doe")
          )
        )
      )

      implicit val request: DataRequest[AnyContent] = dataRequest(validData)

      val result = controller.retrievePartnerName(NormalMode, 0)(success)

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

    "retrieve PSA name for a company" in {

      val companyName = "test company"
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).businessName()

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaName()
      result.value mustBe companyName
    }

    "retrieve PSA name for a partnership" in {

      val partnershipName = "test partnership"
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.Partnership, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).
        businessName("test partnership")

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaName()
      result.value mustBe partnershipName
    }

    "retrieve PSA name for an individual" in {
      val firstName = "first"
      val lastName = "last"
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).
        individualDetails(TolerantIndividual(Some(firstName), None, Some(lastName)))

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Individual, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaName()
      result.value mustBe firstName + " " + lastName
    }
  }

  "psaEmail" must {
    val email = "test@test.com"
    "retrieve PSA email for a company" in {

      val companyName = "test company"
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).
        businessName().companyEmail(email)

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaEmail
      result.value mustBe email
    }

    "retrieve PSA email for a partnership" in {
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.Partnership, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).
        businessName("test partnership").partnershipEmail(email)

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaEmail
      result.value mustBe email
    }

    "retrieve PSA email for an individual" in {
      val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
        RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)).
        individualDetails(TolerantIndividual(Some("test"), None, Some("last"))).individualEmail(email)

      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest("", ""), "",
        PSAUser(UserType.Individual, None, isExistingPSA = false, None), userAnswers)

      val result = controller.psaEmail
      result.value mustBe email
    }
  }
}