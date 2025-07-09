/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.register.routes.InvalidEmailAddressController
import controllers.routes._
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.partnership.PartnershipEmailId
import identifiers.register._
import models.RegistrationCustomerType.UK
import models.RegistrationIdType.UTR
import models.RegistrationLegalStatus.{Individual, Partnership}
import models.UserType.UserType
import models.enumeration.JourneyType
import models.register.{DeclarationWorkingKnowledge, KnownFact, KnownFacts, PsaSubscriptionResponse, RegistrationStatus}
import models.{NormalMode, RegistrationInfo, UserType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http._
import utils.{FakeNavigator, KnownFactsRetrieval}
import views.html.register.declaration

import scala.concurrent.Future

class DeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  private val onwardRoute: Call = IndexController.onPageLoad

  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val validRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
  val email = "test@test.com"
  val businessName = "MyCompany"
  val registrationInfo: RegistrationInfo =
    RegistrationInfo(
      legalStatus = Partnership,
      sapNumber = "",
      noIdentifier = false,
      customerType = UK,
      idType = Some(UTR),
      idNumber = Some("")
    )

  private val data = Json.obj(
    RegistrationInfoId.toString -> registrationInfo,
    BusinessNameId.toString -> businessName
  )

  val view: declaration = app.injector.instanceOf[declaration]

  private val validPsaResponse = PsaSubscriptionResponse("A0123456")
  private val knownFacts =
    Some(KnownFacts(
      Set(KnownFact("PSAID", "test-psa")),
      Set(KnownFact("NINO", "test-nino"))
    ))

  val validData: JsObject =
    Json.obj(
      DeclarationWorkingKnowledgeId.toString -> JsString(DeclarationWorkingKnowledge.values.head.toString)
    )
  val dataRetrieval = new FakeDataRetrievalAction(Some(validData))

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockPensionAdministratorConnector = mock[PensionAdministratorConnector]
  private val mockTaxEnrolmentsConnector = mock[TaxEnrolmentsConnector]
  private val mockKnownFactsRetrieval = mock[KnownFactsRetrieval]
  private val mockEmailConnector = mock[EmailConnector]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  override def beforeEach(): Unit = {
    reset(mockPensionAdministratorConnector, mockEmailConnector)
  }

  "Declaration Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad.url)
    }

    "calling onSubmit" must {

      "redirect to the next page" when {

        "on a valid request and send the email" in {
          val validData = data ++ Json.obj(
            "partnershipContactDetails" -> Json.obj(
              PartnershipEmailId.toString -> email
            )
          )

          when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(validData))
          when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
            .thenReturn(Future.successful(validPsaResponse))
          when(mockEmailConnector.sendEmail(
            eqTo(email),
            any(),
            eqTo(Map("psaName" -> businessName)),
            eqTo(PsaId("A0123456")),
            eqTo(JourneyType.PSA)
          )(any(), any()))
            .thenReturn(Future.successful(EmailSent))
          when(mockKnownFactsRetrieval.retrieve(any())(any()))
            .thenReturn(knownFacts)
          when(mockTaxEnrolmentsConnector.enrol(any(), any())(any(), any(), any(), any()))
            .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(validData)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockEmailConnector, times(1)).sendEmail(eqTo(email), any(),
            eqTo(Map("psaName" -> businessName)), eqTo(PsaId("A0123456")), eqTo(JourneyType.PSA))(any(), any())
        }

        "on a valid request and not send the email" in {
          when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
            .thenReturn(Future.successful(validPsaResponse))
          when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(data))
          when(mockKnownFactsRetrieval.retrieve(any())(any()))
            .thenReturn(knownFacts)
          when(mockTaxEnrolmentsConnector.enrol(any(), any())(any(), any(), any(), any()))
            .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

          val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(data)),
            fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(InvalidEmailAddressController.onPageLoad(RegistrationStatus.Individual).url)
          verify(mockEmailConnector, never).sendEmail(eqTo(email), any(), any(), eqTo(PsaId("A0123456")), eqTo(JourneyType.PSA))(any(), any())
        }
      }

      "redirect to Session Expired" when {
        "no cached data is found" in {
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad.url)
        }

        "known facts cannot be retrieved" in {
          when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
            .thenReturn(Future.successful(validPsaResponse))
          when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(data))
          when(mockKnownFactsRetrieval.retrieve(any())(any()))
            .thenReturn(None)

          val result = controller().onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SessionExpiredController.onPageLoad.url)
        }

        "enrolment is not successful" in {
          when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
            .thenReturn(Future.successful(validPsaResponse))
          when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(data))
          when(mockKnownFactsRetrieval.retrieve(any())(any()))
            .thenReturn(knownFacts)
          when(mockTaxEnrolmentsConnector.enrol(any(), any())(any(), any(), any(), any()))
            .thenReturn(Future.failed(new Exception))

          val result = controller().onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(YourActionWasNotProcessedController.onPageLoad().url)
        }
      }

      "save the answer and PSA Subscription response on a valid request" in {
        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.successful(validPsaResponse))
        when(mockKnownFactsRetrieval.retrieve(any())(any()))
          .thenReturn(knownFacts)
        when(mockTaxEnrolmentsConnector.enrol(any(), any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val result = controller().onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DeclarationId, value = true)
        FakeUserAnswersCacheConnector.verify(PsaSubscriptionResponseId, validPsaResponse)
      }

      "redirect to Duplicate Registration if a registration already exists for the organization" in {
        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.failed(
            UpstreamErrorResponse(
              message = "INVALID_BUSINESS_PARTNER",
              statusCode = FORBIDDEN,
              reportAs = FORBIDDEN
            )
          ))
        val result = controller().onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DuplicateRegistrationController.onPageLoad().url)
      }

      "redirect to Submission Invalid" when {
        "response is BAD_REQUEST from downstream" in {
          when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
            .thenReturn(Future.failed(new BadRequestException("INVALID_PAYLOAD")))

          val result = controller().onSubmit(NormalMode)(validRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SubmissionInvalidController.onPageLoad().url)
        }
      }

      "redirect to Your Action Was Not Processed if ETMP call fails" in {
        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.failed(new Exception))

        val result = controller().onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(YourActionWasNotProcessedController.onPageLoad().url)
      }

      "redirect to Can not  Registration Administrator if a Active PSA exists" in {
        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.failed(
            UpstreamErrorResponse(
              message = "ACTIVE_PSAID",
              statusCode = FORBIDDEN,
              reportAs = FORBIDDEN
            )
          ))
        val result = controller().onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(CannotRegisterAdministratorController.onPageLoad.url)
      }

      "redirect to Can not Registration Administrator if PsaId is invalid" in {
        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.failed(
            UpstreamErrorResponse(
              message = "INVALID_PSAID",
              statusCode = FORBIDDEN,
              reportAs = FORBIDDEN
            )
          ))
        val result = controller().onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(CannotRegisterAdministratorController.onPageLoad.url)
      }
    }

    "redirect to invalidEmailAddress page when PSA enters invalid email address" must {
      "on a valid request and not send the email" in {

        val registrationInfo: RegistrationInfo =
          RegistrationInfo(
            legalStatus = Individual,
            sapNumber = "",
            noIdentifier = false,
            customerType = UK,
            idType = Some(UTR),
            idNumber = Some("")
          )

        val data = Json.obj(
          RegistrationInfoId.toString -> registrationInfo,
          BusinessNameId.toString -> businessName
        )

        when(mockPensionAdministratorConnector.registerPsa(any())(any(), any()))
          .thenReturn(Future.successful(validPsaResponse))
        when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(data))
        when(mockKnownFactsRetrieval.retrieve(any())(any()))
          .thenReturn(knownFacts)
        when(mockTaxEnrolmentsConnector.enrol(any(), any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
        when(mockEmailConnector.sendEmail(
          eqTo(email),
          any(),
          eqTo(Map("psaName" -> businessName)),
          eqTo(PsaId("A0123456")),
          eqTo(JourneyType.PSA)
        )(any(), any()))
          .thenReturn(Future.successful(EmailNotSent))

        val result = controller(dataRetrievalAction = new FakeDataRetrievalAction(Some(data)),
          fakeUserAnswersCacheConnector = mockUserAnswersCacheConnector).onSubmit(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(InvalidEmailAddressController.onPageLoad(RegistrationStatus.Individual).url)
        verify(mockEmailConnector, never).sendEmail(eqTo(email), any(), any(), eqTo(PsaId("A0123456")), eqTo(JourneyType.PSA))(any(), any())
      }

    }

  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = dataRetrieval,
                          userType: UserType = UserType.Organisation,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
                        ): DeclarationController =
    new DeclarationController(
      appConfig = appConfig,
      authenticate = FakeAuthAction(userType),
      allowAccess = FakeAllowAccessProvider(config = frontendAppConfig),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl,
      allowDeclaration = FakeAllowDeclarationActionProvider(),
      navigator = fakeNavigator,
      dataCacheConnector = fakeUserAnswersCacheConnector,
      pensionAdministratorConnector = mockPensionAdministratorConnector,
      knownFactsRetrieval = mockKnownFactsRetrieval,
      enrolments = mockTaxEnrolmentsConnector,
      emailConnector = mockEmailConnector,
      controllerComponents = controllerComponents,
      view = view
    )

  private def viewAsString(): String =
    view(workingKnowledge = true)(fakeRequest, messages).toString

}
