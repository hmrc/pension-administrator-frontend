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

package controllers.deregister

import audit.testdoubles.StubSuccessfulAuditService
import config.FrontendAppConfig
import connectors._
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.deregister.ConfirmStopBeingPsaFormProvider
import models.register.KnownFacts
import models.requests.DataRequest
import models.{Deregistration, IndividualDetails, MinimalPSA}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AnyContent, AnyContentAsFormUrlEncoded, Call, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.PSAConstants.PSA_ACTIVE_RELATIONSHIP_EXISTS
import views.html.deregister.confirmStopBeingPsa

import scala.concurrent.{ExecutionContext, Future}

class ConfirmStopBeingPsaControllerSpec extends ControllerSpecBase with ScalaFutures {

  import ConfirmStopBeingPsaControllerSpec._

  "ConfirmStopBeingPsaController" must {

    "return to session expired if psaName is not present" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to CannotDeregister page if Psa can't be deregistered and no other PSAs are attached to Open schemes" in {
      val result = controller(canDeregister = Deregistration(canDeregister = false, isOtherPsaAttached = false)).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.deregister.routes.MustInviteOthersController.onPageLoad.url)
    }

    "redirect to CannotDeregister page if Psa can't be deregistered and other PSAs are attached to Open schemes" in {
      val result = controller(canDeregister = Deregistration(canDeregister = false, isOtherPsaAttached = true)).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.deregister.routes.CannotDeregisterController.onPageLoad.url)
    }

    "return OK and the correct view for a GET and ensure audit service is successfully called" in {

      val psa = PsaId("A1234567")
      val user = "Fred"
      val request = fakeRequest.withJsonBody(Json.obj(
        "userId" -> user,
        "psaId" -> psa)
      )

      val result = controller(minimalPsaDetailsIndividual).onPageLoad()(request)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()
    }

    "return to you cannot stop being a psa page if psa suspended flag is set in minimal details" in {
      val result = controller(minimalPsaDetailsNoneSuspended).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.deregister.routes.UnableToStopBeingPsaController.onPageLoad.url)
    }

    "return to update address page if psa RLS flag is set in minimal details" in {
      val result = controller(minimalPsaDetailsRLS).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.UpdateContactAddressController.onPageLoad.url)
    }

    "return to update address page if psa deceased flag is set in minimal details" in {
      val result = controller(minimalPsaDetailsDeceased).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(Call("GET", frontendAppConfig.youMustContactHMRCUrl).url)
    }

    "return to session expired if psaName is not present for Post" in {
      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "should display the errors if no selection made" in {

      val result = controller(minimalPsaDetailsIndividual).onSubmit()(fakeRequest)

      status(result) mustBe BAD_REQUEST
    }

    "redirect to the next page on a successful POST when selected true" in {

      val result = controller(minimalPsaDetailsIndividual).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.deregister.routes.SuccessfulDeregistrationController.onPageLoad().url)
    }

    "redirect to the next page and clear the user cache on a successful POST when selected true" in {

      val result = controller(minimalPsaDetailsIndividual).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verifyAllDataRemoved()
    }

    "redirect to the next page on a successful POST when selected false" in {

      val result = controller(minimalPsaDetailsIndividual).onSubmit()(postRequestCancel)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(overviewPage)
    }

    "redirect to CannotDeregisterController if response status is FORBIDDEN and body contains PSA_ACTIVE_RELATIONSHIP_EXISTS" in {
      val request = FakeRequest().withFormUrlEncodedBody("value" -> "true")

      val mockDeregistrationConnector = new DeregistrationConnector {
        override def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
          Future.successful(HttpResponse(FORBIDDEN, Json.obj("reason" -> PSA_ACTIVE_RELATIONSHIP_EXISTS).toString))

        override def canDeRegister(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Deregistration] =
          Future.successful(Deregistration(canDeregister = true, isOtherPsaAttached = false))
      }

      val controller = new ConfirmStopBeingPsaController(
        frontendAppConfig,
        FakeAuthAction(),
        formProvider,
        fakeMinimalPsaConnector(minimalPsaDetailsIndividual),
        mockDeregistrationConnector,
        fakeTaxEnrolmentsConnector,
        fakeAllowAccess(fakeMinimalPsaConnector(minimalPsaDetailsIndividual), frontendAppConfig),
        FakeUserAnswersCacheConnector,
        controllerComponents,
        view
      )

      val result = controller.onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.deregister.routes.CannotDeregisterController.onPageLoad.url)
    }
  }

}

object ConfirmStopBeingPsaControllerSpec extends ControllerSpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val fakeAuditService = new StubSuccessfulAuditService()

  private def overviewPage = frontendAppConfig.schemesOverviewUrl

  private val formProvider = new ConfirmStopBeingPsaFormProvider
  private val form: Form[Boolean] = formProvider()

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private val postRequestCancel: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private def fakeTaxEnrolmentsConnector: TaxEnrolmentsConnector = new TaxEnrolmentsConnector {
    override def enrol(enrolmentKey: String, knownFacts: KnownFacts)
                      (implicit w: Writes[KnownFacts],
                       hc: HeaderCarrier,
                       executionContext: ExecutionContext,
                       request: DataRequest[AnyContent]): Future[HttpResponse] = ???

    override def deEnrol(groupId: String, psaId: String, userId: String)
                        (implicit hc: HeaderCarrier,
                         ec: ExecutionContext,
                         rh: RequestHeader): Future[HttpResponse] = Future.successful(HttpResponse(NO_CONTENT, ""))
  }

  private def fakeDeregistrationConnector(deregistration: Deregistration): DeregistrationConnector = new DeregistrationConnector {
    override def stopBeingPSA(psaId: String)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = Future.successful(HttpResponse(NO_CONTENT, ""))

    override def canDeRegister(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Deregistration] = Future.successful(deregistration)
  }

  private def fakeMinimalPsaConnector(minimalPsaDetailsIndividual: MinimalPSA): MinimalPsaConnector = new MinimalPsaConnector {
    @annotation.nowarn
    override def getMinimalPsaDetails()(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalPSA] = Future.successful(minimalPsaDetailsIndividual)
  }

  private val minimalPsaDetailsIndividual = MinimalPSA(
    "test@test.com", isPsaSuspended = false, None, Some(IndividualDetails("John", Some("Doe"), "Doe")),
    rlsFlag = false, deceasedFlag = false)
  private val minimalPsaDetailsNone = MinimalPSA("test@test.com", isPsaSuspended = false, None, None,
    rlsFlag = false, deceasedFlag = false)
  private val minimalPsaDetailsRLS = MinimalPSA("test@test.com", isPsaSuspended = false, None, None,
    rlsFlag = true, deceasedFlag = false)
  private val minimalPsaDetailsDeceased = MinimalPSA("test@test.com", isPsaSuspended = false, None, None,
    rlsFlag = false, deceasedFlag = true)
  private val minimalPsaDetailsNoneSuspended = MinimalPSA("test@test.com", isPsaSuspended = true, None, None,
    rlsFlag = false, deceasedFlag = false)

  private def fakeAllowAccess(minimalPsaConnector: MinimalPsaConnector,config: FrontendAppConfig): AllowAccessForNonSuspendedUsersAction =
    new AllowAccessForNonSuspendedUsersAction(minimalPsaConnector, config)

  val view: confirmStopBeingPsa = inject[confirmStopBeingPsa]

  private def controller(minimalPsaDetails: MinimalPSA = minimalPsaDetailsNone,
                         canDeregister: Deregistration = Deregistration(canDeregister = true, isOtherPsaAttached = false)) = {
    val minimalDetailsConnector = fakeMinimalPsaConnector(minimalPsaDetails)
    new ConfirmStopBeingPsaController(
      frontendAppConfig,
      FakeAuthAction(),
      formProvider,
      minimalDetailsConnector,
      fakeDeregistrationConnector(canDeregister),
      fakeTaxEnrolmentsConnector,
      fakeAllowAccess(minimalDetailsConnector, frontendAppConfig),
      FakeUserAnswersCacheConnector,
      controllerComponents,
      view
    )
  }

  private def viewAsString(): String =
    view(form, "John Doe Doe")(fakeRequest, messages).toString

}


