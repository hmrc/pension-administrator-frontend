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
import config.FrontendAppConfig
import connectors.IdentityVerificationConnector
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.routes
import identifiers.JourneyId
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.domain
import uk.gov.hmrc.http.{UnauthorizedException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, ExecutionContext}

class AuthActionSpec extends SpecBase with MockitoSugar {

  import AuthActionSpec._

  def allowAccessActionProviderImpl(config: FrontendAppConfig) = new AllowAccessActionProviderImpl(FakeMinimalPsaConnector(), config)

  "Auth Action" when {

    "redirect to scheme overview page" when {
      "already enrolled in PODS, not coming from confirmation" in {
        val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(Call("GET", frontendAppConfig.schemesOverviewUrl).url)
      }
    }

    "the user is a PSP but not a PSA" must {
      "redirect the user to the PSP cant use this service page" in {
        val enrolmentPP = Enrolments(Set(Enrolment("HMRC-PP-ORG", Seq(EnrolmentIdentifier("PPID", psaId)), "")))
        val retrievalResult = authRetrievals(enrolments = enrolmentPP)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.PensionSchemePractitionerController.onPageLoad().url)
      }
    }

    "called for already enrolled User" must {
      val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", psaId)), "")))
      val retrievalResult = authRetrievals(enrolments = enrolmentPODS)
      val fakeUserAnswersConnector = fakeUserAnswersCacheConnector()

      "return OK" when {
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersConnector, fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])

        def controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        "coming from registered psa details" in {
          val result = controller.onPageLoad(UpdateMode)(FakeRequest("GET", controllers.routes.PsaDetailsController.onPageLoad().url))
          status(result) mustBe OK
        }
      }

      "redirect to interceptor page" when {

        "coming from change page when user is suspended" in {
          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersConnector, fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])

          def controller = new Harness(authAction, new AllowAccessActionProviderImpl(FakeMinimalPsaConnector(isSuspended = true), frontendAppConfig))

          val result = controller.onPageLoad(UpdateMode)(FakeRequest("GET", controllers.register.routes.VariationDeclarationController.onPageLoad().url))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.CannotMakeChangesController.onPageLoad().url)
        }

      }

      "throw RuntimeException " when {
        "no Psa id in the enrolment" in {
          val enrolmentPODS = Enrolments(Set(Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSPID", psaId)), "")))
          val retrievals = authRetrievals(enrolments = enrolmentPODS)
          val authAction = new FullAuthentication(fakeAuthConnector(retrievals), frontendAppConfig,
            fakeUserAnswersConnector, fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])

          def controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

          val res = controller.onPageLoad(NormalMode)(FakeRequest("GET", controllers.routes.IndexController.onPageLoad().url))

          ScalaFutures.whenReady(res.failed) { e =>
            e mustBe a[RuntimeException]
          }
        }
      }
    }

    "called for Company user" must {
      "redirect to Manual IV " when {
        "they want to register as Individual" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(frontendAppConfig.identityVerificationFrontend + startIVLink)
        }

        "journey Id is correct and in the cache but no nino returned from IV" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false, "journeyId" -> "test-journey")

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector(ninoOpt = None), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(frontendAppConfig.identityVerificationFrontend + startIVLink)
        }

        "journey Id is not present in url and not in the cache" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector(ninoOpt = None), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(frontendAppConfig.identityVerificationFrontend + startIVLink)
        }
      }

      "return OK, retrieve the nino from iv" when {

        "journey Id is saved in user answers" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false, "journeyId" -> "test-journey")
          val fakeUserAnswers = fakeUserAnswersCacheConnector(userAnswersData)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswers, fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }

        "journey Id is not in user answers but present in url" in {
          val journeyId = "test-journey-id"
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)
          val fakeUserAnswers = fakeUserAnswersCacheConnector(userAnswersData)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswers, fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(FakeRequest("", s"/url?journeyId=$journeyId"))
          status(result) mustBe OK
          fakeUserAnswers.verify(JourneyId, journeyId)
        }
      }

      "return OK" when {
        "the user is non uk user" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(Json.obj("areYouInUK" -> false)), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }

        "user is in UK and wants to register as Organisation" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> true)

          val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }
      }

      "return OK if affinity Group is Organisation" in {
        val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe OK
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new MissingBearerToken)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new BearerTokenExpired)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "redirect the user to the unauthorised page" when {
      "the user doesn't have sufficient enrolments" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientEnrolments)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "the user doesn't have sufficient confidence level" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new InsufficientConfidenceLevel)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "the user used an unaccepted auth provider" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAuthProvider)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "the user has an unsupported affinity group" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedAffinityGroup)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "there is no affinity group" in {
        val retrievalResult = authRetrievals(affinityGroup = None)
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }

      "the user is not an authorised user" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnauthorizedException("Unknown user"))),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
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
        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(FakeRequest("GET", "/foo"))
        status(result) mustBe OK
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the Unauthorised Assistant page" in {
        val authAction = new FullAuthentication(fakeAuthConnector(Future.failed(new UnsupportedCredentialRole)),
          frontendAppConfig, fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedAssistantController.onPageLoad().url)
      }
    }

    "called for user belonging to Agent affinity group" must {

      "redirect to AgentCannotRegister page" in {
        val retrievalResult = authRetrievals(affinityGroup = Some(AffinityGroup.Agent))

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentCannotRegisterController.onPageLoad().url)
      }
    }

    "called for user belonging to Individual affinity group" must {

      "redirect to Use Organisation Credentials page" in {
        val retrievalResult = authRetrievals(affinityGroup = Some(AffinityGroup.Individual))

        val authAction = new FullAuthentication(fakeAuthConnector(retrievalResult), frontendAppConfig,
          fakeUserAnswersCacheConnector(), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
        val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))

        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UseOrganisationCredentialsController.onPageLoad().url)
      }
    }
  }

  "AuthenticationWithNoIV" when {
    "called for Company user" must {
      "return OK and able to view the page and not redirect to IV" when {
        "they want to register as Individual" in {
          val retrievalResult = authRetrievals(ConfidenceLevel.L50, Some(AffinityGroup.Organisation))
          val userAnswersData = Json.obj("areYouInUK" -> true, "registerAsBusiness" -> false)

          val authAction = new AuthenticationWithNoIV(fakeAuthConnector(retrievalResult), frontendAppConfig,
            fakeUserAnswersCacheConnector(userAnswersData), fakeIVConnector(), app.injector.instanceOf[BodyParsers.Default])
          val controller = new Harness(authAction, allowAccess = allowAccessActionProviderImpl(frontendAppConfig))
          val result = controller.onPageLoad()(fakeRequest)
          status(result) mustBe OK
        }
      }
    }
  }
}

object AuthActionSpec {
  private val psaId = "A0000000"
  private val startIVLink = "/start-iv-link"
  private val nino = domain.Nino("AB123456C")

  def fakeUserAnswersCacheConnector(dataToBeReturned: JsValue = Json.obj("areYouInUK" -> true)):
  FakeUserAnswersCacheConnector = new FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        executionContext: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = {
      Future.successful(Some(dataToBeReturned))
    }
  }

  def fakeIVConnector(ninoOpt: Option[domain.Nino] = Some(nino)): IdentityVerificationConnector = new IdentityVerificationConnector {
    override def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                                      (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[String] = {
      Future.successful(startIVLink)
    }

    override def retrieveNinoFromIV(journeyId: String)
                                   (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[domain.Nino]] = {
      Future.successful(ninoOpt)
    }
  }

  private def fakeAuthConnector(stubbedRetrievalResult: Future[_]) = new AuthConnector {

    def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[A] = {
      stubbedRetrievalResult.map(_.asInstanceOf[A])
    }
  }

  private def authRetrievals(confidenceLevel: ConfidenceLevel = ConfidenceLevel.L50,
                             affinityGroup: Option[AffinityGroup] = Some(AffinityGroup.Organisation),
                             enrolments: Enrolments = Enrolments(Set()),
                             creds:Option[Credentials] = Option(Credentials(
                               providerId = "test provider", providerType = ""
                             ))
                            ) = Future.successful(
    new ~(new ~(new ~(new ~(
      Some("id"), confidenceLevel),
      affinityGroup),
      enrolments),
      creds
    )
  )

  class Harness(authAction: AuthAction, allowAccess: AllowAccessActionProvider)
    extends BaseController {
    def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (authAction andThen allowAccess(mode)) { _ => Ok }

    override protected def controllerComponents: ControllerComponents = stubMessagesControllerComponents()
  }

}
