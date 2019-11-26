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

import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.VariationDeclarationFitAndProperFormProvider
import identifiers.register._
import models.UserType.UserType
import models._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import views.html.register.variationDeclarationFitAndProper

class VariationDeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  import VariationDeclarationFitAndProperControllerSpec._

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
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, true)
      }

      "save the answer no on a valid request" in {
        val result = controller().onSubmit(UpdateMode)(fakeRequest.withFormUrlEncodedBody("value" -> "false"))
        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, false)
      }
    }
  }
  app.stop()
}

object VariationDeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new VariationDeclarationFitAndProperFormProvider()()

  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  val view: variationDeclarationFitAndProper = app.injector.instanceOf[variationDeclarationFitAndProper]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          userType: UserType = UserType.Organisation,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
                        ) =
    new VariationDeclarationFitAndProperController(
      appConfig,
      FakeAuthAction(userType),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new VariationDeclarationFitAndProperFormProvider(),
      fakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(psaName: String, form: Form[_]) =
    view(
      form,
      Some(psaName)
    )(fakeRequest, messages).toString

}
