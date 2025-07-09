/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register

import controllers.actions.FakeAuthAction
import models.{CheckMode, UserType}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import views.html.register.invlidEmailAddress
import controllers.ControllerSpecBase
import models.UserType.UserType
import models.register.RegistrationStatus
import play.api.i18n.Messages

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InvalidEmailAddressControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockMcc = Helpers.stubMessagesControllerComponents()
  def view: invlidEmailAddress = app.injector.instanceOf[invlidEmailAddress]
  val userType: UserType = UserType.Organisation

  def controller() = new InvalidEmailAddressController(
    authenticate = FakeAuthAction(userType),
    mockMcc,
    view
  )(global)

  private def viewAsString(redirectUrl: Call, fakeRequest: FakeRequest[?], messages: Messages): String =
    view(redirectUrl)(fakeRequest, messages).toString

  "InvalidEmailAddressController" should {

    "return OK and the correct view for a GET when status is LimitedCompany" in {
      implicit val messages: Messages = stubMessages()
      val request = FakeRequest()
      val result: Future[Result] = controller().onPageLoad(RegistrationStatus.LimitedCompany)(request)
      status(result) mustBe OK
      val redirectUrl = controllers.register.company.routes.CompanyEmailController.onPageLoad(CheckMode)
      contentAsString(result) mustBe viewAsString(redirectUrl, request, messages)
    }

    "return OK and the correct view for a GET when status is Partnership" in {
      implicit val messages: Messages = stubMessages()
      val request = FakeRequest()
      val result: Future[Result] = controller().onPageLoad(RegistrationStatus.Partnership)(request)
      status(result) mustBe OK
      val redirectUrl = controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(CheckMode)
      contentAsString(result) mustBe viewAsString(redirectUrl, request, messages)
    }

    "return OK and the correct view for a GET when status is Individual" in {
      implicit val messages: Messages = stubMessages()
      val request = FakeRequest()
      val result: Future[Result] = controller().onPageLoad(RegistrationStatus.Individual)(FakeRequest())
      status(result) mustBe OK
      val redirectUrl = controllers.register.individual.routes.IndividualEmailController.onPageLoad(CheckMode)
     contentAsString(result) mustBe viewAsString(redirectUrl, request, messages)
    }
  }

}