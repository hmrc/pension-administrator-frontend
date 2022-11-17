/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.administratorPartnership

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.{ControllerSpecBase, IsRegisteredNameControllerBehaviour}
import forms.register.IsRegisteredNameFormProvider
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormViewModel, Message}
import views.html.register.isRegisteredName

class PartnershipIsRegisteredNameControllerSpec extends ControllerSpecBase with IsRegisteredNameControllerBehaviour {

  val name = "test partnership name"

  val view: isRegisteredName = app.injector.instanceOf[isRegisteredName]

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers())

  "PartnershipIsRegisteredNameController" must {

    behave like isRegisteredNameController(viewModel, createController(getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = testController(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = testController(dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

  def viewModel = CommonFormViewModel(
    NormalMode,
    routes.PartnershipIsRegisteredNameController.onSubmit,
    Message("isRegisteredName.partnership.title", name),
    Message("isRegisteredName.partnership.heading", name)
  )


  def testController(
                      dataRetrievalAction: DataRetrievalAction
                    ): PartnershipIsRegisteredNameController =
    createController(dataRetrievalAction)(FakeUserAnswersCacheConnector, FakeNavigator)

  def createController(
                        dataRetrievalAction: DataRetrievalAction
                      )(connector: UserAnswersCacheConnector, nav: Navigator): PartnershipIsRegisteredNameController =
    new PartnershipIsRegisteredNameController(
      appConfig = frontendAppConfig,
      cacheConnector = connector,
      navigator = nav,
      authenticate = FakeAuthAction,
      allowAccess = FakeAllowAccessProvider(config = frontendAppConfig),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl(),
      formProvider = new IsRegisteredNameFormProvider(),
      controllerComponents,
      view
    )

}



