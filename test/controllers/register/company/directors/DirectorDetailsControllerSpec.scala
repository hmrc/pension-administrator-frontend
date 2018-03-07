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

package controllers.register.company.directors

import java.time.LocalDate

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.company.directors.DirectorDetailsFormProvider
import identifiers.register.company.directors.DirectorDetailsId
import models.NormalMode
import models.register.company.directors.DirectorDetails
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.company.directors.directorDetails

class DirectorDetailsControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DirectorDetailsFormProvider()
  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new DirectorDetailsController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  private def viewAsString(index: Int, form: Form[_] = form) =
    directorDetails(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages).toString

  "DirectorDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(0)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        "directors" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString -> DirectorDetails("John", None, "Doe", LocalDate.now())
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, 0)(fakeRequest)

      contentAsString(result) mustBe viewAsString(0, form.fill(DirectorDetails("John", None, "Doe", LocalDate.now())))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("firstName", "John"),
        ("lastName", "Doe"),
        ("dateOfBirth.day", "9"),
        ("dateOfBirth.month", "6"),
        ("dateOfBirth.year", "1862")
      )

      val result = controller().onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the director details when valid data is submittd" in {
      // scalastyle:off magic.number
      val dob = LocalDate.of(1862, 6, 9)
      // scalastyle:on magic.number

      val director = DirectorDetails(
        "John",
        Some("J"),
        "Doe",
        dob
      )

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("firstName", director.firstName),
        ("middleName", director.middleName.get),
        ("lastName", director.lastName),
        ("dateOfBirth.day", director.dateOfBirth.getDayOfMonth.toString),
        ("dateOfBirth.month", director.dateOfBirth.getMonthValue.toString),
        ("dateOfBirth.year", director.dateOfBirth.getYear.toString)
      )

      val result = controller().onSubmit(NormalMode, 1)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verify(DirectorDetailsId(1), director)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val invalidValue = Seq.fill(DirectorDetailsFormProvider.firstNameLength + 1)("A").mkString
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", invalidValue))
      val boundForm = form.bind(Map("firstName" -> invalidValue))

      val result = controller().onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(0, boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Doe"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
