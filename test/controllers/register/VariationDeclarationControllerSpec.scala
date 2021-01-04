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

import connectors.PensionsSchemeConnector
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{DeclarationFitAndProperId, DeclarationId, VariationWorkingKnowledgeId}
import models.UserType.UserType
import models.register.PsaSubscriptionResponse
import models.{NormalMode, TolerantIndividual, UpdateMode, UserType}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import views.html.register.variationDeclaration

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationControllerSpec extends ControllerSpecBase {

  private val psaId = "test psa ID"

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val href = controllers.register.routes.VariationDeclarationController.onClickAgree()

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .set(VariationWorkingKnowledgeId)(true).asOpt.value
    .set(DeclarationFitAndProperId)(true).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))
  private val fakePensionsSchemeConnector: FakePensionsSchemeConnector = new FakePensionsSchemeConnector()

  "DeclarationVariationController" when {

    "calling onPageLoad" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "calling onAgreeAndContinue" must {

      "save the answer and redirect to the next page" in {
        val result = controller().onClickAgree(UpdateMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeUserAnswersCacheConnector.verify(DeclarationId, true)
      }

      "call the update psa method on the pensions connector with correct psa ID and user answers data" in {
        fakePensionsSchemeConnector.reset()
        val result = controller().onClickAgree(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        fakePensionsSchemeConnector.updateCalledWithData mustBe Some((psaId, UserAnswers(Json
          .parse("""{"declaration":true,"existingPSA":{"isExistingPSA":false},"declarationWorkingKnowledge":"workingKnowledge"}"""))))
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onClickAgree(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }

  class FakePensionsSchemeConnector extends PensionsSchemeConnector {
    def reset(): Unit = updateCalledWithData = None

    var updateCalledWithData: Option[(String, UserAnswers)] = None

    override def registerPsa(answers: UserAnswers)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[PsaSubscriptionResponse] = ???

    override def updatePsa(psaId: String, answers: UserAnswers)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] = {
      updateCalledWithData = Some((psaId, answers))
      Future.successful(HttpResponse(OK, ""))
    }
  }

  val view: variationDeclaration = app.injector.instanceOf[variationDeclaration]

  private def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalAction,
                         userType: UserType = UserType.Organisation) =
    new VariationDeclarationController(
      frontendAppConfig,
      FakeAuthAction(userType, psaId),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      FakeUserAnswersCacheConnector,
      fakePensionsSchemeConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(): String =
    view(None, true, href)(fakeRequest, messages).toString

}


