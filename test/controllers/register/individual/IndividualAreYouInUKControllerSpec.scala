/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.register.individual

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.*
import identifiers.register.AreYouInUKId
import models.admin.ukResidencyToggle
import models.{Mode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.navigators.IndividualNavigatorV2
import utils.{FakeNavigator, FeatureFlagMockHelper, UserAnswers}
import viewmodels.{AreYouInUKViewModel, Message}
import views.html.register.areYouInUK

class IndividualAreYouInUKControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach with FeatureFlagMockHelper {

  private val onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new forms.register.YesNoFormProvider()
  private val form: Form[Boolean] = formProvider()
  private val navigatorV2 = mock[IndividualNavigatorV2]

  val view: areYouInUK = app.injector.instanceOf[areYouInUK]

  private def viewmodel(mode: Mode) =
    AreYouInUKViewModel(
      mode,
      postCall = routes.IndividualAreYouInUKController.onSubmitIndividual(mode),
      title = Message("areYouInUKIndividual.title"),
      heading = Message("areYouInUKIndividual.heading")
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    featureFlagMock(ukResidencyToggle)
    reset(navigatorV2)
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new IndividualAreYouInUKController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      navigatorV2,
      FakeAllowAccessProvider(config = frontendAppConfig),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockFeatureFlagService,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[Boolean] = form) =
    view(form, viewmodel(NormalMode))(fakeRequest, messages).toString

  "Individual AreYouInUK Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(AreYouInUKId.toString -> true)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the next page for POST when toggle is disabled" in {
      val data = new FakeDataRetrievalAction(Some(Json.obj()))

      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "true")
      val result = controller(data).onSubmitIndividual(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the v2 navigator when toggle is enabled" in {
      featureFlagMock(ukResidencyToggle, true)
      val data = new FakeDataRetrievalAction(Some(Json.obj()))
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "true")

      when(navigatorV2.nextPage(any(), any(), any())).thenReturn(onwardRoute)

      val result = controller(data).onSubmitIndividual(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to NonUKAdministratorController if value is false" in {
      val data = new FakeDataRetrievalAction(Some(Json.obj()))
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "false")

      val result = controller(data).onSubmitIndividual(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.individual.routes.NonUKAdministratorController.onPageLoad().url)
    }

    "return Bad Request when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "xxx")
      val boundForm = form.bind(Map("value" -> "xxx"))

      val result = controller().onSubmitIndividual(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}