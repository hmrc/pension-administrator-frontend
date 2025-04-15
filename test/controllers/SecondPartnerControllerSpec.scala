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

package controllers

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import forms.register.YesNoFormProvider
import models.UserType.Organisation
import models.{PersonName, UpdateMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import services.PsaDetailsService
import utils.{FakeNavigator, UserAnswers}
import views.html.secondPartner

import scala.concurrent.Future

class SecondPartnerControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val partnerName = "test partner"
  val view: secondPartner = app.injector.instanceOf[secondPartner]
  val formProvider = new YesNoFormProvider()
  val form: Form[Boolean] = formProvider("secondPartner.error")
  private val validData = UserAnswers().partnerName(0, PersonName("test", "partner"))
  private val mockPsaDetailsService = mock[PsaDetailsService]

  private def postCall: Call = routes.SecondPartnerController.onSubmit()
  private def onwardRoute: Call = controllers.register.partnership.routes.AddPartnerController.onSubmit(UpdateMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new SecondPartnerController(new FakeNavigator(desiredRoute = onwardRoute), new FakeAuthAction(Organisation),
      FakeAllowAccessProvider(config = frontendAppConfig), dataRetrievalAction, formProvider, mockPsaDetailsService,
      FakeUserAnswersCacheConnector, controllerComponents, view)

  def viewAsString(form: Form[?] = form): String = view(form, Some(partnerName), postCall)(fakeRequest, messages).toString

  override protected def beforeEach(): Unit = {
    reset(mockPsaDetailsService)
    when(mockPsaDetailsService.getUserAnswers(any(), any())(any(), any())).thenReturn(Future.successful(validData))
  }

  "SecondPartnerController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "set the answer to yes and redirect" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid data"))
      val boundForm = form.bind(Map("value" -> "invalid data"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}

