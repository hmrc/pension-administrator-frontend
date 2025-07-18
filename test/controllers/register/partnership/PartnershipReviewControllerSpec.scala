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

package controllers.register.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.BusinessNameId
import identifiers.register.partnership.partners.PartnerNameId
import models.{NormalMode, PersonName}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.partnership.partnershipReview

class PartnershipReviewControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val partnershipName = "test partnership name"
  val partners: Seq[String] = Seq("partner a", "partner b", "partner c")

  val view: partnershipReview = app.injector.instanceOf[partnershipReview]

  def partner(lastName: String, isDeleted: Boolean = false): JsObject = Json.obj(
    PartnerNameId.toString -> PersonName("partner", lastName, isDeleted)
  )

  val validData: JsObject = Json.obj(
    BusinessNameId.toString ->
      partnershipName,
    "partners" -> Json.arr(partner("a"), partner("b"), partner("c"), partner("d", isDeleted = true))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new PartnershipReviewController(
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  private def viewAsString() = view(partnershipName, partners)(fakeRequest, messages).toString

  "PartnershipReview Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page on submit" in {
      val result = controller().onSubmit()(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
