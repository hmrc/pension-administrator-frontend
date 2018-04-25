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

package controllers.register

import play.api.data.Form
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers.{contentAsString, _}
import views.html.register.declarationFitAndProper
import controllers.ControllerSpecBase
import forms.register.DeclarationFormProvider
import identifiers.register.DeclarationFitAndProperId
import models.UserType
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperControllerSpec extends ControllerSpecBase {

  import DeclarationFitAndProperControllerSpec._

  "DeclarationFitAndProperController" when {

    "calling GET" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "set cancel link correctly to Individual What You Will Need page" in {
        val result = controller(userType = UserType.Individual).onPageLoad()(FakeRequest())

        contentAsString(result) mustBe viewAsString(cancelCall = individualCancelCall)
      }

      "set cancel link correctly to Company What You Will Need page" in {
        val result = controller().onPageLoad()(fakeRequest)

        contentAsString(result) mustBe viewAsString(cancelCall = companyCancelCall)
      }
    }

    "calling POST" must {

      "redirect to the next page on a valid request" in {
        val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
        val result = controller().onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "save the answer on a valid request" in {
        val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
        val result = controller().onSubmit(request)

        status(result) mustBe SEE_OTHER
        FakeDataCacheConnector.verify(DeclarationFitAndProperId, true)
      }

      "reject an invalid request and display errors" in {
        val formWithErrors = form.withError("agree", messages("declaration.invalid"))
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(formWithErrors)
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

      "set cancel link correctly to Individual What You Will Need page" in {
        val formWithErrors = form.withError("agree", messages("declaration.invalid"))
        val result = controller(userType = UserType.Individual).onSubmit()(fakeRequest)

        contentAsString(result) mustBe viewAsString(formWithErrors, individualCancelCall)
      }

      "set cancel link correctly to Company What You Will Need page" in {
        val formWithErrors = form.withError("agree", messages("declaration.invalid"))
        val result = controller().onSubmit()(fakeRequest)

        contentAsString(result) mustBe viewAsString(formWithErrors, companyCancelCall)
      }
    }
  }
}

object DeclarationFitAndProperControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new DeclarationFormProvider()()
  private val companyCancelCall = controllers.register.company.routes.WhatYouWillNeedController.onPageLoad()
  private val individualCancelCall = controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad()

  private def fakeAuthAction(userType: UserType) = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", userType, false))
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, userType: UserType = UserType.Organisation) =
    new DeclarationFitAndProperController(
      frontendAppConfig,
      messagesApi,
      fakeAuthAction(userType),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new DeclarationFormProvider(),
      FakeDataCacheConnector
    )

  private def viewAsString(form: Form[_] = form, cancelCall: Call = companyCancelCall) =
    declarationFitAndProper(
      frontendAppConfig,
      form,
      cancelCall
    )(fakeRequest, messages).toString

}
