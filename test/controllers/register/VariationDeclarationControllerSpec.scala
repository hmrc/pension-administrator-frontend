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

import connectors.{FakeUserAnswersCacheConnector, PensionsSchemeConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.VariationDeclarationFormProvider
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{DeclarationFitAndProperId, DeclarationId, VariationWorkingKnowledgeId}
import models.UserType.UserType
import models.{NormalMode, TolerantIndividual, UserType}
import models.register.{DeclarationWorkingKnowledge, PsaSubscriptionResponse}
import models.{NormalMode, TolerantIndividual, UpdateMode, UserType}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, UserAnswers}
import views.html.register.variationDeclaration

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationControllerSpec extends ControllerSpecBase {

  import VariationDeclarationControllerSpec._

  "DeclarationVariationController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a valid POST request" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      val result = controller().onSubmit(UpdateMode)(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the answer on a valid POST request" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      val result = controller().onSubmit(NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(DeclarationId, true)
    }

    "call the update psa method on the pensions connector on a valid POST request with correct psa ID and user answers data" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      fakePensionsSchemeConnector.reset()
      val result = controller().onSubmit(NormalMode)(request)
      status(result) mustBe SEE_OTHER
      fakePensionsSchemeConnector.updateCalledWithData mustBe Some((psaId, UserAnswers(Json
        .parse("""{"declaration":true}"""))))
    }

    "reject an invalid POST request and display errors" in {
      val formWithErrors = form.withError("agree", messages("declaration.variations.invalid"))
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors)
    }

    "redirect to Session Expired on a POST request if no cached data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object VariationDeclarationControllerSpec extends ControllerSpecBase {

  private val psaId = "test psa ID"

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new VariationDeclarationFormProvider()()

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .set(VariationWorkingKnowledgeId)(true).asOpt.value
    .set(DeclarationFitAndProperId)(true).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  class FakePensionsSchemeConnector extends PensionsSchemeConnector {
    def reset():Unit = updateCalledWithData = None
    var updateCalledWithData: Option[(String,UserAnswers)] = None
    override def registerPsa(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = ???
    override def updatePsa(psaId:String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
      updateCalledWithData = Some((psaId, answers))
      Future.successful(())
    }
  }

  private val fakePensionsSchemeConnector: FakePensionsSchemeConnector = new FakePensionsSchemeConnector()

  private def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalAction,
                         userType: UserType = UserType.Organisation) =
    new VariationDeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction(userType, psaId),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new VariationDeclarationFormProvider(),
      FakeUserAnswersCacheConnector,
      fakePensionsSchemeConnector
    )

  private def viewAsString(form: Form[_] = form) =
    variationDeclaration(frontendAppConfig, form, "", true)(fakeRequest, messages).toString

}


