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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.routes
import identifiers.AdministratorOrPractitionerId
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar {

  import AuthActionSpec._

  private val minimalPsa = MinimalPSA(
    email = "a@a.c",
    isPsaSuspended = false,
    organisationName = None,
    individualDetails = None,
    rlsFlag = false,
    deceasedFlag = false
  )

  def allowAccessActionProviderImpl(config: FrontendAppConfig) = new AllowAccessActionProviderImpl(FakeMinimalPsaConnector(minimalPsa), config)

  "Auth Action" when {
    "the user is a PSP but not a PSA" must {
      "redirect the user to the PSP cant use this service page" in {
        val enrolmentPP = Enrolments(Set(Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPP)
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PensionSchemePractitionerController.onPageLoad().url)
      }
    }

    "the user has enrolled in PODS as both a PSA AND a PSP" must {
      "have access to PSA page when he has chosen to act as a PSA" in {
        val optionUAJson = UserAnswers()
          .set(AdministratorOrPractitionerId)(AdministratorOrPractitioner.Administrator).asOpt.map(_.json)
        when(mockSessionDataCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(optionUAJson))
        val retrievalResult = authRetrievals(enrolments = bothEnrolments)
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])

        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }

      "redirect to cannot access as practitioner when trying to access PSA page when chosen to act as a PSP" in {
        val optionUAJson = UserAnswers()
          .set(AdministratorOrPractitionerId)(AdministratorOrPractitioner.Practitioner).asOpt.map(_.json)
        when(mockSessionDataCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(optionUAJson))
        val retrievalResult = authRetrievals(enrolments = bothEnrolments)
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])

        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.cannotAccessPageAsPractitionerUrl(frontendAppConfig.localFriendlyUrl(fakeRequest.uri)))
      }

      "redirect to administrator or practitioner page when trying to access PSA page when not chosen a role" in {
        val optionUAJson = Some(Json.obj())
        when(mockSessionDataCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(optionUAJson))
        val retrievalResult = authRetrievals(enrolments = bothEnrolments)
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])

        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.administratorOrPractitionerUrl)
      }
    }

    "called for already enrolled User" must {
      val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
      val retrievalResult = authRetrievals(enrolments = enrolmentPODS)

      "return OK" when {
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
          mockSessionDataCacheConnector, app.injector.instanceOf[BodyParsers.Default])

        def controller = new Harness(authAction)

        "coming from registered psa details" in {
          val result = controller.onPageLoad()(FakeRequest("GET", controllers.routes.PsaDetailsController.onPageLoad().url))
          status(result) mustBe OK
        }
      }

      "throw RuntimeException " when {
        "no Psa id in the enrolment" in {
          val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSPID", psaId)), "")))
          val retrievals = authRetrievals(enrolments = enrolmentPODS)
          val authAction = new AuthenticationAction(fakeAuthConnector(retrievals), frontendAppConfig,
            mockSessionDataCacheConnector, app.injector.instanceOf[BodyParsers.Default])

          def controller = new Harness(authAction)

          val res = controller.onPageLoad()(FakeRequest("GET", controllers.routes.IndexController.onPageLoad.url))

          ScalaFutures.whenReady(res.failed) { e =>
            e mustBe a[RuntimeException]
          }
        }
      }
    }

    "called for Company user" must {
      "return OK" when {
        "the user is non uk user" in {
          val retrievalResult = authRetrievals(Some(AffinityGroup.Organisation))

          val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig, mockSessionDataCacheConnector,
            app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }

        "user is in UK and wants to register as Organisation" in {
          val retrievalResult = authRetrievals(Some(AffinityGroup.Organisation))

          val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
            mockSessionDataCacheConnector,
            app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }
      }

      "return OK if affinity Group is Organisation" in {
        val retrievalResult = authRetrievals(Some(AffinityGroup.Organisation))

        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new MissingBearerToken)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new BearerTokenExpired)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "redirect the user to the unauthorised page" when {
      "the user doesn't have sufficient enrolments" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new InsufficientEnrolments)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }

      "the user doesn't have sufficient confidence level" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new InsufficientConfidenceLevel)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest("GET", "/test"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          """
            |http://localhost:9938/mdtp/uplift?origin=pods&confidenceLevel=250&completionURL=/test&failureURL=/register-as-pension-scheme-administrator/unauthorised
            |""".stripMargin)
      }

      "the user used an unaccepted auth provider" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new UnsupportedAuthProvider)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }

      "the user has an unsupported affinity group" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new UnsupportedAffinityGroup)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }

      "there is no affinity group" in {
        val retrievalResult = authRetrievals(affinityGroup = None)
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
      }

      "the user is not an authorised user" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new UnauthorizedException("Unknown user"))),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
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
        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe OK
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the Unauthorised Assistant page" in {
        val authAction = new AuthenticationAction(fakeAuthConnector(Future.failed(new UnsupportedCredentialRole)),
          frontendAppConfig,  mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedAssistantController.onPageLoad().url)
      }
    }

    "called for user belonging to Agent affinity group" must {

      "redirect to AgentCannotRegister page" in {
        val retrievalResult = authRetrievals(affinityGroup = Some(AffinityGroup.Agent))

        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentCannotRegisterController.onPageLoad.url)
      }
    }

    "called for user belonging to Individual affinity group" must {

      "redirect to Use Organisation Credentials page" in {
        val retrievalResult = authRetrievals(affinityGroup = Some(AffinityGroup.Individual))

        val authAction = new AuthenticationAction(fakeAuthConnector(retrievalResult), frontendAppConfig,
           mockSessionDataCacheConnector,
          app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UseOrganisationCredentialsController.onPageLoad.url)
      }
    }
  }

  "AuthenticationWithNoIV" when {
    "called for Company user" must {
      "return OK and able to view the page and not redirect to IV" when {
        "they want to register as Individual" in {
          val retrievalResult = authRetrievals(Some(AffinityGroup.Organisation))

          val authAction = new AuthenticationWithNoIV(fakeAuthConnector(retrievalResult), frontendAppConfig,
            mockSessionDataCacheConnector, app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }
      }
    }
  }
}

object AuthActionSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {
  private val psaId = "A0000000"

  private val mockSessionDataCacheConnector = mock[SessionDataCacheConnector]

  private val enrolmentPSP = Enrolment(
    key = "HMRC-PODSPP-ORG",
    identifiers = Seq(EnrolmentIdentifier(key = "PSPID", value = "20000000")),
    state = "",
    delegatedAuthRule = None
  )

  private val enrolmentPSA = Enrolment(
    key = "HMRC-PODS-ORG",
    identifiers = Seq(EnrolmentIdentifier(key = "PSAID", value = "A0000000")),
    state = "",
    delegatedAuthRule = None
  )

  private val bothEnrolments = Enrolments(Set(enrolmentPSA, enrolmentPSP))

  def fakeUserAnswersCacheConnector(dataToBeReturned: JsValue = Json.obj("areYouInUK" -> true)):
  FakeUserAnswersCacheConnector = new FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        executionContext: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = {
      Future.successful(Some(dataToBeReturned))
    }
  }

  private def fakeAuthConnector(stubbedRetrievalResult: Future[_]) = new AuthConnector {

    def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[A] = {
      stubbedRetrievalResult.map(_.asInstanceOf[A])(executionContext)
    }
  }

  private def authRetrievals(affinityGroup: Option[AffinityGroup] = Some(AffinityGroup.Organisation),
                             enrolments: Enrolments = Enrolments(Set()),
                             creds: Option[Credentials] = Option(Credentials(
                               providerId = "test provider", providerType = ""
                             )),
                             groupId: Option[String] = Some("test-group-id"),
                             nino: Option[String] = Some("AA000003D")
                            ) = Future.successful(
    new~(new~(new~(new~(new~(
      Some("id"),
      affinityGroup),
      enrolments),
      creds),
      groupId),
      nino)
  )

  class Harness(authAction: AuthAction)
    extends BaseController {
    def onPageLoad: Action[AnyContent] = authAction { _ => Ok }

    override protected def controllerComponents: ControllerComponents = SpecBase.controllerComponents
  }

  override def beforeEach(): Unit = {
    reset(mockSessionDataCacheConnector)
    super.beforeEach()
  }

}
