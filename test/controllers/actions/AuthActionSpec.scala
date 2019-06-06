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

package controllers.actions

import java.net.URLEncoder

import base.SpecBase
import config.FeatureSwitchManagementService
import connectors.{FakeUserAnswersCacheConnector, IdentityVerificationConnector}
import controllers.routes
import identifiers.JourneyId
import models._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Controller
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar {

  import AuthActionSpec._

  "Auth Action" when {

    "redirect to scheme overview page" when {
      "already enrolled in PODS, not coming from confirmation" in {
        val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.InterceptPSAController.onPageLoad().url)
      }
    }

    "the user is a PSP but not a PSA" must {
      "redirect the user to the PSP cant use this service page" in {
        val enrolmentPP = Enrolments(Set(Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPP)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PensionSchemePractitionerController.onPageLoad().url)
      }
    }

    "called for already enrolled User" must {
      val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
      val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
      val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()


      "Return OK and interact correctly with min details connector" when {
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersConnector, fakeIVConnector)

        def controller = new Harness(authAction)

        "coming from duplicate registration" in {
          val result = controller.onPageLoad(UpdateMode)(FakeRequest("GET", controllers.register.routes.DuplicateRegistrationController.onPageLoad().url))
          status(result) mustBe OK
        }
      }

      "return OK" when {
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersConnector, fakeIVConnector)

        def controller = new Harness(authAction)

        "coming from registered psa details" in {
          val result = controller.onPageLoad(UpdateMode)(FakeRequest("GET", controllers.routes.PsaDetailsController.onPageLoad().url))
          status(result) mustBe OK
        }
      }
      "redirect to interceptor page" when {

        "coming from change page when user is suspended" in {
          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswersConnector, fakeIVConnector)
          def controller = new Harness(authAction, new AllowAccessActionProviderImpl(FakeMinimalPsaConnector(isSuspended = true)))

          val result = controller.onPageLoad(UpdateMode)(FakeRequest("GET", controllers.register.routes.VariationDeclarationController.onPageLoad().url))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.CannotMakeChangesController.onPageLoad().url)
        }

      }
    }

    "called for Individual UK user " must {

      "return OK if they have Confidence level 200 or higher and not enrolled" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L200, AffinityGroup.Individual)
        val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersConnector, fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }

      "redirect to IV if they have confidence level less than 200" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Individual)
        val redirectUrl = s"${frontendAppConfig.ivUpliftUrl}?origin=PODS&" +
          s"completionURL=${URLEncoder.encode(frontendAppConfig.ukJourneyContinueUrl, "UTF-8")}&" +
          s"failureURL=${URLEncoder.encode(s"${frontendAppConfig.loginContinueUrl}/unauthorised", "UTF-8")}" +
          s"&confidenceLevel=${ConfidenceLevel.L200.level}"
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(redirectUrl)
      }
    }

    "called for Individual user " must {

      "return OK if they have confidence level less than 200 and they have not answered if they are UK/NON-UK" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L100, AffinityGroup.Individual)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(Json.obj()), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "called for Company user" must {
      "redirect to Manual IV " when {
        "they want to register as Individual" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(frontendAppConfig.identityVerificationFrontend + startIVLink)
        }
      }

      "return OK, retrieve the nino from iv" when {

        "journey Id is saved in user answers" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false, "journeyId" -> "test-journey")
          val fakeUserAnswers = fakeUserAnswersCacheConnector(userAnswersData)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswers, fakeIVConnector)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }

        "journey Id is not in user answers but present in url" in {
          val journeyId = "test-journey-id"
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)
          val fakeUserAnswers = fakeUserAnswersCacheConnector(userAnswersData)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswers, fakeIVConnector)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest("", s"/url?journeyId=$journeyId"))
          status(result) mustBe OK
          fakeUserAnswers.verify(JourneyId, journeyId)
        }
      }

      "return OK" when {
        "the user is non uk user" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswersCacheConnector(Json.obj("areYouInUK" -> false)), fakeIVConnector)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }

        "user is in UK and wants to register as Organisation" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> true)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }
      }

      "return OK if affinity Group is Organisation" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new MissingBearerToken)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new BearerTokenExpired)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientEnrolments)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user is a PSP and is a PSA" must {
      "do something" in {
        val enrolmentsPSA = Enrolments(
          Set(
            Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", psaId)), ""),
            Enrolment("HMRC-PSA-ORG", Seq(EnrolmentIdentifier("PSAID", "A000000")), "")
          )
        )
        val retrievalResult = authRetrievals(enrolments = enrolmentsPSA)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe OK
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientConfidenceLevel)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAuthProvider)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAffinityGroup)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the Unauthorised Assistant page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedCredentialRole)),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedAssistantController.onPageLoad().url)
      }
    }

    "called for Individual UK user and annotated with AuthenticationForLowConfidence" must {

      "return OK if they have confidence level less than 200" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Individual)
        val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()
        val authAction = new AuthenticationWithNoConfidence(fakeAuthConnector(retrievalResult),
          frontendAppConfig, fakeFeatureSwitchManagerService(), fakeUserAnswersConnector, fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "called for user belonging to Agent affinity group" must {

      "redirect to AgentCannotRegister page" in {
        val retrievalResult = authRetrievals(affinityGroup = AffinityGroup.Agent)

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeFeatureSwitchManagerService(),
          fakeUserAnswersCacheConnector(), fakeIVConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentCannotRegisterController.onPageLoad().url)
      }
    }
  }

}

object AuthActionSpec {
  private val psaId = "A0000000"
  private val startIVLink = "/start-iv-link"
  private val nino = "test-nino"

  def fakeUserAnswersCacheConnector(dataToBeReturned: JsValue = Json.obj("areYouInUK" -> true)):
  FakeUserAnswersCacheConnector = new FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        ec: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = {
      Future.successful(Some(dataToBeReturned))
    }
  }

  def fakeFeatureSwitchManagerService(isIvEnabled: Boolean = true) = new FeatureSwitchManagementService {
    override def change(name: String, newValue: Boolean): Boolean = ???

    override def get(name: String): Boolean = isIvEnabled

    override def reset(name: String): Unit = ???
  }

  def fakeIVConnector: IdentityVerificationConnector = new IdentityVerificationConnector {
    override def startRegisterOrganisationAsIndividual(completionURL: String,
                                                       failureURL: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
      Future.successful(startIVLink)
    }

    override def retrieveNinoFromIV(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
      Future.successful(Some(nino))
    }
  }

  private def fakeAuthConnector(stubbedRetrievalResult: Future[_]) = new AuthConnector {

    def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
      stubbedRetrievalResult.map(_.asInstanceOf[A])
    }
  }

  private def authRetrievals(confidenceLevel: ConfidenceLevel = ConfidenceLevel.L50,
                             affinityGroup: AffinityGroup = AffinityGroup.Organisation,
                             enrolments: Enrolments = Enrolments(Set())) = Future.successful(new ~(new ~(new ~(new ~(
    Some("id"), confidenceLevel),
    Some(affinityGroup)),
    Some("nino")),
    enrolments)
  )

  class Harness(authAction: AuthAction, allowAccess: AllowAccessActionProvider = new AllowAccessActionProviderImpl(FakeMinimalPsaConnector())) extends Controller {
    def onPageLoad(mode: Mode = NormalMode) = (authAction andThen allowAccess(mode)) { _ => Ok }
  }
  
}
