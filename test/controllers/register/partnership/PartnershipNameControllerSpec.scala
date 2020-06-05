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

package controllers.register.partnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, FakeAllowAccessProvider, FakeAuthAction, FakeDataRetrievalAction}
import controllers.register.BusinessNameControllerBehaviour
import forms.BusinessNameFormProvider
import identifiers.register.BusinessTypeId
import models.register.BusinessType
import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import views.html.register.businessName

class PartnershipNameControllerSpec extends ControllerSpecBase with BusinessNameControllerBehaviour {

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers())

  def validData = UserAnswers(Json.obj(
      BusinessTypeId.toString -> BusinessType.LimitedPartnership.toString
    ))

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  override val view: businessName = app.injector.instanceOf[businessName]

  def createController(userAnswers: UserAnswers): PartnershipNameController =
    new PartnershipNameController(
      frontendAppConfig,
      new FakeUserAnswersCacheConnector{},
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      new FakeDataRetrievalAction(Some(userAnswers.json)),
      new DataRequiredActionImpl(),
      new BusinessNameFormProvider(),
      stubMessagesControllerComponents(),
      view
    ){
      override def href: Call = onwardRoute
    }

  val formProvider = new BusinessNameFormProvider()
  val form: Form[String] = formProvider(
    requiredKey = "partnershipName.error.required",
    invalidKey = "partnershipName.error.invalid",
    lengthKey = "partnershipName.error.length")

  "PartnershipUTRController" must {

    behave like businessNameController(validData, createController, form)
  }

}
