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

package controllers.vary

import config.FrontendAppConfig
import connectors._
import controllers.ControllerSpecBase
import controllers.actions._
import forms.vary.DeclarationFitAndProperFormProvider
import identifiers.register._
import models.UserType.UserType
import models._
import models.register.{KnownFact, KnownFacts, PsaSubscriptionResponse}
import models.requests.{AuthenticatedRequest, DataRequest}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Writes
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}
import utils.{FakeNavigator, KnownFactsRetrieval, UserAnswers}
import views.html.vary.declarationFitAndProper

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  import DeclarationFitAndProperControllerSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "DeclarationFitAndProperController (variations)" when {

    "calling GET" must {

      "return OK and the correct view for a company" in {
        val result = controller(dataRetrievalAction = getCompany).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(form = form, psaName = "Test Company Name")
      }

      "return OK and the correct view for an individual" in {
        val result = controller(dataRetrievalAction = getIndividual, userType = UserType.Individual).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(form = form, psaName = "TestFirstName TestLastName")
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "calling POST" must {
      "save the answer yes on a valid request and redirect to Session Expired" in {
        val result = controller().onSubmit(UpdateMode)(fakeRequest.withFormUrlEncodedBody("value" -> "true"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, true)
      }

      "save the answer no on a valid request" in {
        val result = controller().onSubmit(UpdateMode)(fakeRequest.withFormUrlEncodedBody("value" -> "false"))
        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, false)
      }
    }
  }
}

object DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new DeclarationFitAndProperFormProvider()()

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

  private val mockEmailConnector = mock[EmailConnector]
  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          userType: UserType = UserType.Organisation,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
                          pensionsSchemeConnector: PensionsSchemeConnector = fakePensionsSchemeConnector,
                          knownFactsRetrieval: KnownFactsRetrieval = fakeKnownFactsRetrieval(),
                          enrolments: TaxEnrolmentsConnector = fakeEnrolmentStoreConnector()
                        ) =
    new DeclarationFitAndProperController(
      appConfig,
      messagesApi,
      fakeAuthAction(userType),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new DeclarationFitAndProperFormProvider(),
      fakeUserAnswersCacheConnector,
      pensionsSchemeConnector,
      knownFactsRetrieval,
      enrolments,
      mockEmailConnector
    )

  private def viewAsString(psaName: String, form: Form[_]) =
    declarationFitAndProper(
      frontendAppConfig,
      form,
      psaName
    )(fakeRequest, messages).toString

}
