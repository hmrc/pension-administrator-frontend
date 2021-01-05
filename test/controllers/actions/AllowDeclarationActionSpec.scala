/*
 * Copyright 2021 HM Revenue & Customs
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
import models.requests.OptionalDataRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Result
import play.api.test.Helpers._
import utils.UserAnswers
import utils.dataCompletion.DataCompletion
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowDeclarationActionSpec extends SpecBase with ScalaFutures {

  private val mockDataCompletion = mock[DataCompletion]

  class TestAllowDeclarationAction(mode: Mode, isSuspended: Boolean = false) extends AllowDeclarationAction(mode, mockDataCompletion) {
    override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = super.filter(request)
  }

  private def optionalRequest(ua: UserAnswers) =
    OptionalDataRequest(fakeRequest, "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None), Some(ua))

  "AllowDeclarationAction" must {

    "allow access to declaration pages when all the data is complete for individual and adviser" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual)
      when(mockDataCompletion.isIndividualComplete(any(), any())).thenReturn(true)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe None
      }
    }

    "allow access to declaration pages when all the data is complete for company and adviser" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.LimitedCompany)
      when(mockDataCompletion.isCompanyComplete(any(), any())).thenReturn(true)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe None
      }
    }

    "allow access to declaration pages when all the data is complete for partnership and adviser" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.Partnership)
      when(mockDataCompletion.isPartnershipComplete(any(), any())).thenReturn(true)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe None
      }
    }

    "allow access to declaration pages when no data for reg info and adviser is complete" in {
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(UserAnswers()))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe None
      }
    }

    "redirect to register as business page when all the data is complete for individual but adviser is not complete" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual)
      when(mockDataCompletion.isIndividualComplete(any(), any())).thenReturn(true)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(false)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }
    }

    "redirect to register as business page when all the data is complete for adviser, but individual is incomplete" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual)
      when(mockDataCompletion.isIndividualComplete(any(), any())).thenReturn(true)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(false)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }
    }

    "redirect to register as business page when all the data is complete for adviser, but company is incomplete" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.LimitedCompany)
      when(mockDataCompletion.isCompanyComplete(any(), any())).thenReturn(false)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }
    }

    "redirect to register as business page when all the data is complete for adviser, but partnership is incomplete" in {
      val ua = UserAnswers().regInfo(RegistrationLegalStatus.Partnership)
      when(mockDataCompletion.isPartnershipComplete(any(), any())).thenReturn(false)
      when(mockDataCompletion.isAdviserComplete(any(), any())).thenReturn(true)
      val action = new TestAllowDeclarationAction(NormalMode)
      val futureResult = action.filter(optionalRequest(ua))

      whenReady(futureResult) { result =>
        result.map {
          _.header.status
        } mustBe Some(SEE_OTHER)
        result.flatMap {
          _.header.headers.get(LOCATION)
        } mustBe Some(controllers.register.routes.RegisterAsBusinessController.onPageLoad().url)
      }
    }
  }
}
