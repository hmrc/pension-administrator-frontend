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

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.vary.DeclarationVariationFormProvider
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{DeclarationFitAndProperId, DeclarationId, DeclarationWorkingKnowledgeId}
import models.UserType.UserType
import models.register.DeclarationWorkingKnowledge
import models.requests.AuthenticatedRequest
import models.{NormalMode, PSAUser, TolerantIndividual, UserType}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.vary.declarationVariation

import scala.concurrent.Future

class DeclarationVariationControllerSpec extends ControllerSpecBase {

  import DeclarationVariationControllerSpec._

  "DeclarationVariationController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a valid POST request" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      val result = controller().onSubmit(NormalMode)(request)

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

object DeclarationVariationControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new DeclarationVariationFormProvider()()

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge).asOpt.value
    .set(DeclarationFitAndProperId)(true).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  private def fakeAuthAction(userType: UserType) = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", PSAUser(userType, None, false, None)))
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalAction,
                         userType: UserType = UserType.Organisation) =
    new DeclarationVariationController(
      frontendAppConfig,
      messagesApi,
      fakeAuthAction(userType),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new DeclarationVariationFormProvider(),
      FakeUserAnswersCacheConnector
    )

  private def viewAsString(form: Form[_] = form) =
    declarationVariation(frontendAppConfig, form, "", true, true)(fakeRequest, messages).toString

}


