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

package controllers.register

import config.FrontendAppConfig
import connectors._
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.routes._
import identifiers.register.partnership.PartnershipEmailId
import identifiers.register.{BusinessNameId, PsaSubscriptionResponseId, RegistrationInfoId, _}
import models.RegistrationCustomerType.UK
import models.RegistrationIdType.UTR
import models.RegistrationLegalStatus.Partnership
import models.UserType.UserType
import models.register.{DeclarationWorkingKnowledge, KnownFact, KnownFacts, PsaSubscriptionResponse}
import models.requests.DataRequest
import models.{BusinessDetails, NormalMode, RegistrationInfo, UserType}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsString, Json, Writes}
import play.api.mvc.{AnyContent, RequestHeader}
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, KnownFactsRetrieval, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val validRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
  val businessDetails: BusinessDetails = BusinessDetails("MyCompany", Some("1234567890"))
  val email = "test@test.com"
  val businessName = "MyCompany"
  val registrationInfo: RegistrationInfo = RegistrationInfo(Partnership, "", noIdentifier = false, UK, Some(UTR), Some(""))
  private val data = Json.obj(RegistrationInfoId.toString -> registrationInfo,
    BusinessNameId.toString -> businessName
  )

  val view: declaration = app.injector.instanceOf[declaration]

  private val validPsaResponse = PsaSubscriptionResponse("A0123456")
  private val knownFacts = Some(KnownFacts(
    Set(KnownFact("PSAID", "test-psa")),
    Set(KnownFact("NINO", "test-nino")
    )))

  val validData: JsObject = Json.obj(DeclarationWorkingKnowledgeId.toString -> JsString(DeclarationWorkingKnowledge.values.head.toString))
  val dataRetrieval = new FakeDataRetrievalAction(Some(validData))

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "Declaration Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
    }

    "calling onSubmit" must {

      "redirect to the next page" when {

        "on a valid request and send the email" in {
          val validData = data ++ Json.obj(
            "partnershipContactDetails" -> Json.obj(
              PartnershipEmailId.toString -> email
            )
          )

          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
          when(mockEmailConnector.sendEmail(eqTo(email), any(), eqTo(Map("psaName" -> businessName)),
            eqTo(PsaId("A0123456")))(any(), any())).thenReturn(Future.successful(EmailSent))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(validData)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, times(1)).sendEmail(eqTo(email), any(),
            eqTo(Map("psaName" -> businessName)), eqTo(PsaId("A0123456")))(any(), any())
        }

        "on a valid request and not send the email" in {
          reset(mockEmailConnector)
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(data)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, never()).sendEmail(eqTo(email), any(), any(), eqTo(PsaId("A0123456")))(any(), any())
        }
      }

      "redirect to Session Expired" when {
        "no cached data is found" in {
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
        }

        "known facts cannot be retrieved" in {
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector,
            knownFactsRetrieval = fakeKnownFactsRetrieval(None)).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
        }

        "enrolment is not successful" in {
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector,
            enrolments = fakeEnrolmentStoreConnector(HttpResponse(BAD_REQUEST, ""))
          ).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad().url)
        }
      }

      "save the answer and PSA Subscription response on a valid request" in {
        val result = controller().onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DeclarationId, value = true)
        FakeUserAnswersCacheConnector.verify(PsaSubscriptionResponseId, validPsaResponse)
      }

      "redirect to Duplicate Registration if a registration already exists for the organization" in {
        val result = controller(pensionsSchemeConnector = fakePensionsSchemeConnector(
          Future.failed(UpstreamErrorResponse(message = "INVALID_BUSINESS_PARTNER", statusCode = FORBIDDEN, reportAs = FORBIDDEN))))
          .onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DuplicateRegistrationController.onPageLoad().url)
      }

      "redirect to Submission Invalid" when {
        "response is BAD_REQUEST from downstream" in {
          val result = controller(pensionsSchemeConnector = fakePensionsSchemeConnector(
            Future.failed(new BadRequestException("INVALID_PAYLOAD"))))
            .onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SubmissionInvalidController.onPageLoad().url)
        }
      }
    }
  }


  private def fakePensionsSchemeConnector(response: Future[PsaSubscriptionResponse] = Future.successful(validPsaResponse)): PensionsSchemeConnector =
    new PensionsSchemeConnector {

      override def registerPsa(answers: UserAnswers)
                              (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[PsaSubscriptionResponse] = {
        response
      }

      override def updatePsa(psaId: String, answers: UserAnswers
                            )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] =
        Future.successful(HttpResponse(OK, ""))
    }

  private def fakeKnownFactsRetrieval(knownFacts: Option[KnownFacts] = knownFacts) = new KnownFactsRetrieval {
    override def retrieve(psaId: String)(implicit request: DataRequest[AnyContent]): Option[KnownFacts] = knownFacts
  }

  private def fakeEnrolmentStoreConnector(enrolResponse: HttpResponse = HttpResponse(NO_CONTENT, "")): TaxEnrolmentsConnector = {
    new TaxEnrolmentsConnector {
      override def deEnrol(groupId: String, psaId: String, userId: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext, rh: RequestHeader): Future[HttpResponse] = ???

      override def enrol(enrolmentKey: String, knownFacts: KnownFacts
                        )(implicit w: Writes[KnownFacts],
                          hc: HeaderCarrier,
                          executionContext: ExecutionContext,
                          request: DataRequest[AnyContent]): Future[HttpResponse] =
        enrolResponse.status match {
          case NO_CONTENT => Future.successful(enrolResponse)
          case ex => Future.failed(new HttpException("Fail", ex))
        }
    }

  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = dataRetrieval,
                          userType: UserType = UserType.Organisation,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
                          pensionsSchemeConnector: PensionsSchemeConnector = fakePensionsSchemeConnector(),
                          knownFactsRetrieval: KnownFactsRetrieval = fakeKnownFactsRetrieval(),
                          enrolments: TaxEnrolmentsConnector = fakeEnrolmentStoreConnector()
                        ): DeclarationController =
    new DeclarationController(
      appConfig,
      FakeAuthAction(userType),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeAllowDeclarationActionProvider(),
      fakeNavigator,
      fakeUserAnswersCacheConnector,
      pensionsSchemeConnector,
      knownFactsRetrieval,
      enrolments,
      mockEmailConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(): String =
    view(workingKnowledge = true)(fakeRequest, messages).toString

}
