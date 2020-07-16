/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.BusinessNameId
import identifiers.register.company.directors.DirectorNameId
import models.{NormalMode, PersonName}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import views.html.register.company.companyReview

class CompanyReviewControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val companyName = "test company name"
  val directors: Seq[String] = Seq("director a", "director b", "director c")

  val view: companyReview = app.injector.instanceOf[companyReview]

  def director(lastName: String, isDeleted: Boolean = false): JsObject = Json.obj(
    DirectorNameId.toString -> PersonName("director", lastName, isDeleted)
  )

  val validData: JsObject = Json.obj(
    BusinessNameId.toString ->
      companyName,
    "directors" -> Json.arr(director("a"), director("b"), director("c"), director("d", isDeleted = true))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyReviewController(
      frontendAppConfig,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString() = view(companyName, directors)(fakeRequest, messages).toString

  "CompanyReview Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
