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

package controllers.register

import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.register._
import identifiers.register.partnership.PartnershipEmailId
import models.RegistrationCustomerType.UK
import models.RegistrationIdType.UTR
import models.RegistrationLegalStatus.Partnership
import models.UserType.UserType
import models._
import models.register.{KnownFact, KnownFacts, PsaSubscriptionResponse}
import models.requests.DataRequest
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{Writes, _}
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpException, HttpResponse, Upstream4xxResponse}
import utils.{FakeNavigator, KnownFactsRetrieval, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  import DeclarationFitAndProperControllerSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "DeclarationFitAndProperController" when {

    "calling onPageLoad" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "set cancel link correctly to Individual What You Will Need page" in {
        val result = controller(userType = UserType.Individual).onPageLoad(NormalMode)(FakeRequest())

        contentAsString(result) mustBe viewAsString(cancelCall = individualCancelCall)
      }

      "set cancel link correctly to Company What You Will Need page" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        contentAsString(result) mustBe viewAsString(cancelCall = companyCancelCall)
      }
    }

    "calling onClickAgree" must {

      "redirect to the next page" when {

        "on a valid request and send the email" in {
          val validData = data ++ Json.obj(
            "partnershipContactDetails" -> Json.obj(
              PartnershipEmailId.toString -> email
            )
          )

          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
          when(mockEmailConnector.sendEmail(eqTo(email), any(), eqTo(PsaId("A0123456")))(any(), any())).thenReturn(Future.successful(EmailSent))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(validData)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onClickAgree(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, times(1)).sendEmail(eqTo(email), any(), eqTo(PsaId("A0123456")))(any(), any())
        }

        "on a valid request and not send the email" in {
          reset(mockEmailConnector)
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(data)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onClickAgree(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, never()).sendEmail(eqTo(email), any(), eqTo(PsaId("A0123456")))(any(), any())
        }
      }

      "redirect to Session Expired" when {
        "no cached data is found" in {
          val result = controller(dontGetAnyData).onClickAgree(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "known facts cannot be retrieved" in {
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector,
            knownFactsRetrieval = fakeKnownFactsRetrieval(None)).onClickAgree(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "enrolment is not successful" in {
          when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(data))
          val result = controller(
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector,
            enrolments = fakeEnrolmentStoreConnector(HttpResponse(BAD_REQUEST))
          ).onClickAgree(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }

      "save the answer and PSA Subscription response on a valid request" in {
        val result = controller().onClickAgree(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, value = true)
        FakeUserAnswersCacheConnector.verify(PsaSubscriptionResponseId, validPsaResponse)
      }

      "redirect to Duplicate Registration if a registration already exists for the organization" in {
        val result = controller(pensionsSchemeConnector = fakePensionsSchemeConnector(
          Future.failed(Upstream4xxResponse(message = "INVALID_BUSINESS_PARTNER", upstreamResponseCode = FORBIDDEN, reportAs = FORBIDDEN))))
          .onClickAgree(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.DuplicateRegistrationController.onPageLoad().url)
      }

      "redirect to Submission Invalid" when {
        "response is BAD_REQUEST from downstream" in {
          val result = controller(pensionsSchemeConnector = fakePensionsSchemeConnector(
            Future.failed(new BadRequestException("INVALID_PAYLOAD"))))
            .onClickAgree(NormalMode)(validRequest)

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
  private val validRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
  val businessDetails = BusinessDetails("MyCompany", Some("1234567890"))
  val email = "test@test.com"
  val businessName = "MyCompany"
  val registrationInfo = RegistrationInfo(Partnership, "", false, UK, Some(UTR), Some(""))
  private val data = Json.obj(RegistrationInfoId.toString -> registrationInfo,
    BusinessNameId.toString -> businessName
  )

  private val validPsaResponse = PsaSubscriptionResponse("A0123456")
  private val knownFacts = Some(KnownFacts(
    Set(KnownFact("PSAID", "test-psa")),
    Set(KnownFact("NINO", "test-nino")
    )))

  private def fakePensionsSchemeConnector(response: Future[PsaSubscriptionResponse] = Future.successful(validPsaResponse)): PensionsSchemeConnector =
    new PensionsSchemeConnector {

      override def registerPsa
      (answers: UserAnswers)
      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = {
        response
      }

      override def updatePsa(psaId: String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = ???
    }

  private def fakeKnownFactsRetrieval(knownFacts: Option[KnownFacts] = knownFacts) = new KnownFactsRetrieval {
    override def retrieve(psaId: String)(implicit request: DataRequest[AnyContent]): Option[KnownFacts] = knownFacts
  }

  private def fakeEnrolmentStoreConnector(enrolResponse: HttpResponse = HttpResponse(NO_CONTENT)): TaxEnrolmentsConnector = {
    new TaxEnrolmentsConnector {
      override def enrol(enrolmentKey: String, knownFacts: KnownFacts)(implicit w: Writes[KnownFacts],
                                                                       hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]) =
        enrolResponse.status match {
          case NO_CONTENT => Future.successful(enrolResponse)
          case ex => Future.failed(new HttpException("Fail", ex))
        }
    }

  }

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]
  private val hrefCall = controllers.register.routes.DeclarationFitAndProperController.onClickAgree()

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          userType: UserType = UserType.Organisation,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
                          pensionsSchemeConnector: PensionsSchemeConnector = fakePensionsSchemeConnector(),
                          knownFactsRetrieval: KnownFactsRetrieval = fakeKnownFactsRetrieval(),
                          enrolments: TaxEnrolmentsConnector = fakeEnrolmentStoreConnector()
                        ) =
    new DeclarationFitAndProperController(
      appConfig,
      messagesApi,
      FakeAuthAction(userType),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      fakeUserAnswersCacheConnector,
      pensionsSchemeConnector,
      knownFactsRetrieval,
      enrolments,
      mockEmailConnector
    )

  private def viewAsString(form: Form[_] = form, cancelCall: Call = companyCancelCall) =
    declarationFitAndProper(
      frontendAppConfig,
      cancelCall,
      hrefCall
    )(fakeRequest, messages).toString

}
