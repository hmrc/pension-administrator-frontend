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

package controllers.register.company.directors

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.UniqueTaxReferenceFormProvider
import identifiers.register.company.CompanyDetailsId
import identifiers.register.company.directors.{DirectorDetailsId, DirectorUniqueTaxReferenceId}
import models._
import models.register.company.CompanyDetails
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.company.directors.directorUniqueTaxReference

class DirectorUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new UniqueTaxReferenceFormProvider()
  private val form = formProvider("directorUniqueTaxReference.error.required", "directorUniqueTaxReference.error.reason.required")
  private val index = Index(0)
  private val directorName = "test first name test middle name test last name"
  private val companyName = "ThisCompanyName"


  private val validData = Json.obj(
    CompanyDetailsId.toString -> CompanyDetails(None, None),
    "directors" -> Json.arr(
      Json.obj(
        DirectorDetailsId.toString ->
          PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now),
        DirectorUniqueTaxReferenceId.toString ->
          UniqueTaxReference.Yes("1234567891")
      ),
      Json.obj(
        DirectorDetailsId.toString ->
          PersonDetails("test", Some("test"), "test", LocalDate.now)
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new DirectorUniqueTaxReferenceController(frontendAppConfig, messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String = directorUniqueTaxReference(
    frontendAppConfig,
    form,
    NormalMode,
    index,
    directorName
  )(fakeRequest, messages).toString

  "DirectorUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to the next page" when {
      "valid data is submitted with yes selected" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("utr.hasUtr", "true"), ("utr.utr", "1234567890"))

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "valid data is submitted with no selected" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("utr.hasUtr", "false"), ("utr.reason", "test reason"))

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "POST" in {

          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", Nino.options.head.value))
          val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }

      "the index is not valid" in {
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad(NormalMode, Index(2))(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
