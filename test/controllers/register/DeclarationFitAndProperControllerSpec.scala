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

package controllers.register

import connectors._
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.register.{DeclarationFitAndProperId, PsaSubscriptionResponseId}
import models.UserType.UserType
import models.register.{KnownFact, KnownFacts, PsaSubscriptionResponse}
import models.requests.{AuthenticatedRequest, DataRequest}
import models.{PSAUser, UserType}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{Writes, _}
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}
import utils.{FakeNavigator, KnownFactsRetrieval, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  import DeclarationFitAndProperControllerSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "DeclarationFitAndProperController" when {

    "calling GET" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "set cancel link correctly to Individual What You Will Need page" in {
        val result = controller(userType = UserType.Individual).onPageLoad()(FakeRequest())

        contentAsString(result) mustBe viewAsString(cancelCall = individualCancelCall)
      }

      "set cancel link correctly to Company What You Will Need page" in {
        val result = controller().onPageLoad()(fakeRequest)

        contentAsString(result) mustBe viewAsString(cancelCall = companyCancelCall)
      }
    }

    "calling POST" must {

      val data = Json.obj("registrationInfo" -> Json.obj(
        "legalStatus" -> "Partnership",
        "sapNumber" -> "24325236",
        "noIdentifier" -> false,
        "customerType" -> "UK",
        "idType" -> "UTR",
        "idNumber" -> ""
      )
      )
      "redirect to the next page" when {
        "on a valid request and send the email" in {
          val validData = data ++ Json.obj(
            "partnershipContactDetails" -> Json.obj(
              "email" -> "test@test.com",
              "phone" -> "222222"
            )
          )
          val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
          when(mockDataCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
          when(mockEmailConnector.sendEmail(eqTo("test@test.com"), any(), eqTo(PsaId("A0123456")))(any(), any())).thenReturn(Future.successful(EmailSent))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(validData)),
            fakeDataCacheConnector = mockDataCacheConnector).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, times(1)).sendEmail(eqTo("test@test.com"), any(), eqTo(PsaId("A0123456")))(any(), any())
        }

        "on a valid request and not send the email" in {
          reset(mockEmailConnector)
          val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
          when(mockDataCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Json.obj()))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(data)),
            fakeDataCacheConnector = mockDataCacheConnector).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, never()).sendEmail(eqTo("test@test.com"), any(), eqTo(PsaId("A0123456")))(any(), any())
        }
      }

      "save the answer on a valid request" in {
        val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
        val result = controller().onSubmit(request)

        status(result) mustBe SEE_OTHER
        FakeDataCacheConnector.verify(DeclarationFitAndProperId, true)
      }

      "reject an invalid request and display errors" in {
        val formWithErrors = form.withError("agree", messages("declaration.invalid"))
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(formWithErrors)
      }

      "redirect to Session Expired" when {
        "no cached data is found" in {
          val result = controller(dontGetAnyData).onSubmit(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "known facts cannot be retrieved" in {
          val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
          val result = controller(knownFactsRetrieval = fakeKnownFactsRetrieval(None)).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "enrolment is not successful" in {
          val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
          val result = controller(
            enrolments = fakeEnrolmentStoreConnector(HttpResponse(BAD_REQUEST))
          ).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }

      "set cancel link to What You Will Need page" when {

        "Individual" in {
          val formWithErrors = form.withError("agree", messages("declaration.invalid"))
          val result = controller(userType = UserType.Individual).onSubmit()(fakeRequest)

          contentAsString(result) mustBe viewAsString(formWithErrors, individualCancelCall)
        }

        "Company" in {
          val formWithErrors = form.withError("agree", messages("declaration.invalid"))
          val result = controller().onSubmit()(fakeRequest)

          contentAsString(result) mustBe viewAsString(formWithErrors, companyCancelCall)
        }
      }

      "save the PSA Subscription response on a valid request" in {
        val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
        val result = controller().onSubmit(request)

        status(result) mustBe SEE_OTHER
        FakeDataCacheConnector.verify(PsaSubscriptionResponseId, validPsaResponse)
      }

      "redirect to Duplicate Registration if a registration already exists for the organization" in {
        val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
        val result = controller(pensionsSchemeConnector = duplicateRegistrationPensionsSchemeConnector).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.DuplicateRegistrationController.onPageLoad().url)
      }

      "redirect to Submission Invalid" when {
        "response is BAD_REQUEST from downstream" in {
          val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
          val result = controller(pensionsSchemeConnector = submissionInvalidPensionsSchemeConnector).onSubmit(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.register.routes.SubmissionInvalidController.onPageLoad().url)
        }
      }
    }
  }
}

object DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new DeclarationFormProvider()()
  private val companyCancelCall = controllers.register.company.routes.WhatYouWillNeedController.onPageLoad()
  private val individualCancelCall = controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad()

  private def fakeAuthAction(userType: UserType) = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", PSAUser(userType, None, true, Some("test psa id"))))
  }

  private val validPsaResponse = PsaSubscriptionResponse("A0123456")
  private val knownFacts = Some(KnownFacts(
    Set(KnownFact("PSAID", "test-psa")),
    Set(KnownFact("NINO", "test-nino")
    )))

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerPsa
    (answers: UserAnswers)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = {
      Future.successful(validPsaResponse)
    }
  }

  private val duplicateRegistrationPensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerPsa
    (answers: UserAnswers)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = {
      Future.failed(InvalidBusinessPartnerException())
    }
  }

  private val submissionInvalidPensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerPsa
    (answers: UserAnswers)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = {
      Future.failed(InvalidPayloadException())
    }
  }

  private def fakeKnownFactsRetrieval(knownFacts: Option[KnownFacts] = knownFacts) = new KnownFactsRetrieval {
    override def retrieve(psaId: String)(implicit request: DataRequest[AnyContent]): Option[KnownFacts] = knownFacts
  }

  private def fakeEnrolmentStoreConnector(enrolResponse: HttpResponse = HttpResponse(NO_CONTENT)): TaxEnrolmentsConnector = {
    new TaxEnrolmentsConnector {
      override def enrol(enrolmentKey: String, knownFacts: KnownFacts)(implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext) =
        enrolResponse.status match {
          case NO_CONTENT => Future.successful(enrolResponse)
          case ex => Future.failed(new HttpException("Fail", ex))
        }
    }
  }

  val mockDataCacheConnector = mock[DataCacheConnector]
  val mockEmailConnector = mock[EmailConnector]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          userType: UserType = UserType.Organisation,
                          fakeDataCacheConnector: DataCacheConnector = FakeDataCacheConnector,
                          pensionsSchemeConnector: PensionsSchemeConnector = fakePensionsSchemeConnector,
                          knownFactsRetrieval: KnownFactsRetrieval = fakeKnownFactsRetrieval(),
                          enrolments: TaxEnrolmentsConnector = fakeEnrolmentStoreConnector()
                        ) =
    new DeclarationFitAndProperController(
      frontendAppConfig,
      messagesApi,
      fakeAuthAction(userType),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new DeclarationFormProvider(),
      fakeDataCacheConnector,
      pensionsSchemeConnector,
      knownFactsRetrieval,
      enrolments,
      mockEmailConnector
    )

  private def viewAsString(form: Form[_] = form, cancelCall: Call = companyCancelCall) =
    declarationFitAndProper(
      frontendAppConfig,
      form,
      cancelCall
    )(fakeRequest, messages).toString

}
