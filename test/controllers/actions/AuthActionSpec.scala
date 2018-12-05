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

package controllers.actions

import java.net.URLEncoder

import base.SpecBase
import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import controllers.routes
import identifiers.PsaId
import play.api.inject.guice.GuiceApplicationBuilder
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

class AuthActionSpec extends SpecBase {

  import AuthActionSpec._

  "Auth Action" when {

    "redirect to scheme overview page" when {
      "already enrolled in PODS, not coming from confirmation" in {
        val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
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
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PensionSchemePractitionerController.onPageLoad().url)
      }
    }

    "called for already enrolled User" must {
      "return OK and save the PsaId" when {
        val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
        val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersConnector)
        val controller = new Harness(authAction)

        "coming from confirmation" in {
          val result = controller.onPageLoad()(FakeRequest("GET", frontendAppConfig.confirmationUri))
          status(result) mustBe OK
          fakeUserAnswersConnector.verify(PsaId, psaId)
        }

        "coming from duplicate registration" in {
          val result = controller.onPageLoad()(FakeRequest("GET", frontendAppConfig.duplicateRegUri))
          status(result) mustBe OK
          fakeUserAnswersConnector.verify(PsaId, psaId)
        }

        "coming from registered psa details" in {
          val result = controller.onPageLoad()(FakeRequest("GET", frontendAppConfig.registeredPsaDetailsUri))
          status(result) mustBe OK
          fakeUserAnswersConnector.verify(PsaId, psaId)
        }
      }
    }

    "called for Individual UK user " must {

      "return OK if they have Confidence level 200 or higher and not enrolled" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L200, AffinityGroup.Individual)
        val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersConnector)
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
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(redirectUrl)
      }
    }

    "called for Individual user " must {

      "return OK if they have confidence level less than 200 and they have not answered if they are UK/NON-UK" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L100, AffinityGroup.Individual)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector(None))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "called for Company user" must {

      "return OK if affinity Group is Organisation" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Organisation)

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }

      "redirect the user to Unauthorised page if the affinity group is not Individual/Company " in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, AffinityGroup.Agent)

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new MissingBearerToken)), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new BearerTokenExpired)), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientEnrolments)), frontendAppConfig, fakeUserAnswersCacheConnector())
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
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe OK
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientConfidenceLevel)),
          frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAuthProvider)), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAffinityGroup)), frontendAppConfig, fakeUserAnswersCacheConnector())
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the Unauthorised Assistant page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedCredentialRole)), frontendAppConfig, fakeUserAnswersCacheConnector())
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
        val authAction = new AuthenticationWithNoConfidence(fakeAuthConnector(retrievalResult), frontendAppConfig, fakeUserAnswersConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }
  }

}

object AuthActionSpec {
  private val psaId = "A0000000"

  def fakeUserAnswersCacheConnector(isInUk: Option[Boolean] = Some(true)): FakeUserAnswersCacheConnector = new FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        ec: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = {

      isInUk match {
        case Some(flag) => Future.successful(Some(Json.obj("areYouInUK" -> flag)))
        case _ => Future.successful(Some(Json.obj()))
      }
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

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad() = authAction { _ => Ok }
  }

}
