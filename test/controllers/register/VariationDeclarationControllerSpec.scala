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

import audit.testdoubles.StubSuccessfulAuditService
import config.FrontendAppConfig
import connectors.cache.FakeUserAnswersCacheConnector
import connectors.{EmailConnector, EmailSent, PensionAdministratorConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.routes._
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipEmailId
import identifiers.register.{BusinessNameId, DeclarationFitAndProperId, RegistrationInfoId, VariationWorkingKnowledgeId}
import models.RegistrationCustomerType.UK
import models.RegistrationIdType.UTR
import models.RegistrationLegalStatus.Partnership
import models.UserType.UserType
import models.{NormalMode, RegistrationInfo, TolerantIndividual, UserType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.{FakeNavigator, UserAnswers}
import views.html.register.variationDeclaration

import scala.concurrent.Future

class VariationDeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

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

  private val psaId = "test psa ID"
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .set(VariationWorkingKnowledgeId)(true).asOpt.value
    .set(DeclarationFitAndProperId)(true).asOpt.value
    .set(BusinessNameId)(businessName).asOpt.value
    .set(RegistrationInfoId)(registrationInfo).asOpt.value
    .set(PartnershipEmailId)(email).asOpt.value

    val view: variationDeclaration = app.injector.instanceOf[variationDeclaration]


  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))
  private val mockConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]
  private val mockEmailConnectorA = mock[EmailConnector]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = dataRetrievalAction,
                          userType: UserType                       = UserType.Organisation
                        ) =
    new VariationDeclarationController(
      appConfig               = mock[FrontendAppConfig],
      authenticate            = FakeAuthAction(userType, psaId),
      allowAccess             = FakeAllowAccessProvider(config = frontendAppConfig),
      getData                 = dataRetrievalAction,
      requireData             = new DataRequiredActionImpl,
      navigator               = fakeNavigator,
      dataCacheConnector      = FakeUserAnswersCacheConnector,
      pensionAdministratorConnector = mockConnector,
      emailConnector          = mockEmailConnectorA,
      auditService            = new StubSuccessfulAuditService(),
      controllerComponents    = controllerComponents,
      view                    = view
    )


  private def viewAsString(): String =
    view(
      psaNameOpt        = None,
      isWorkingKnowldge = true,
      href              = VariationDeclarationController.onClickAgree()
    )(fakeRequest, messages).toString

  override def beforeEach(): Unit = {
    reset(mockConnector, mockEmailConnectorA)
  }

  "DeclarationVariationController" when {

//    "calling onPageLoad" must {
//
//      "return OK and the correct view" in {
//        val result = controller().onPageLoad(UpdateMode)(fakeRequest)
//
//        status(result) mustBe OK
//        contentAsString(result) mustBe viewAsString()
//      }
//
//      "redirect to Session Expired if no cached data is found" in {
//        val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
//    }

    "calling onAgreeAndContinue" must {

//      "save the answer and redirect to the next page" in {
//        when(mockConnector.updatePsa(any(), any())(any(), any()))
//          .thenReturn(Future.successful(HttpResponse(200, "")))
//
//
//        val result = controller().onClickAgree(UpdateMode)(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//
//        redirectLocation(result) mustBe Some(onwardRoute.url)
//
//        FakeUserAnswersCacheConnector.verify(DeclarationId, true)
//      }

      "call the update psa method on the pensions connector with correct psa ID and user answers data" in {
        when(mockConnector.updatePsa(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))
        when(mockEmailConnectorA.sendEmail(
          any(),
          any(),
          any(),
          any()
        )(any(), any()))
          .thenThrow(new RuntimeException("Errorr!!!"))
          //.thenReturn(Future.successful(EmailSent))

        val result = controller().onClickAgree(NormalMode)(fakeRequest)

        val answers =
          UserAnswers(Json.parse(
            """{
              | "declaration": true,
              | "existingPSA": {
              |   "isExistingPSA": false
              | },
              | "declarationWorkingKnowledge": "workingKnowledge"
              |}""".stripMargin
          ))

        status(result) mustBe SEE_OTHER

        verify(mockConnector, times(1)).updatePsa(eqTo(psaId), eqTo(answers))(any(), any())
//        verify(mockEmailConnector, times(1)).sendEmail(any(), any(),
//          any(), any())(any(), any())
      }

//      "redirect to Your Action Was Not Processed if ETMP call fails" in {
//        when(mockConnector.updatePsa(any(), any())(any(), any()))
//          .thenReturn(Future.failed(new Exception))
//
//        val result = controller().onClickAgree(NormalMode)(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//
//        redirectLocation(result) mustBe Some(controllers.routes.YourActionWasNotProcessedController.onPageLoad().url)
//      }
//
//      "redirect to Session Expired if no cached data is found" in {
//        val result = controller(dontGetAnyData).onClickAgree(NormalMode)(fakeRequest)
//
//        status(result) mustBe SEE_OTHER
//
//        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//      }
    }
  }
}


