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

package controllers.register.company

import connectors.cache.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{ControllerSpecBase, IsRegisteredNameControllerBehaviour}
import forms.register.IsRegisteredNameFormProvider
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormViewModel, Message}

class CompanyIsRegisteredNameControllerSpec extends ControllerSpecBase with IsRegisteredNameControllerBehaviour {

  import CompanyIsRegisteredNameControllerSpec._

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers())

  "CompanyIsRegisteredNameController" must {

    behave like isRegisteredNameController(viewModel, createController(this, getEmptyData))

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = testController(this, dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = testController(this, dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object CompanyIsRegisteredNameControllerSpec {

  def viewModel = CommonFormViewModel(
    NormalMode,
    routes.CompanyIsRegisteredNameController.onSubmit(),
    Message("isRegisteredName.company.title", name),
    Message("isRegisteredName.company.heading", name)
  )

  val name = "test company name"

  def testController(
                      base: ControllerSpecBase,
                      dataRetrievalAction: DataRetrievalAction
                    ): CompanyIsRegisteredNameController =
    createController(base, dataRetrievalAction)(FakeUserAnswersCacheConnector, FakeNavigator)

  def createController(
                        base: ControllerSpecBase,
                        dataRetrievalAction: DataRetrievalAction
                      )(connector: UserAnswersCacheConnector, nav: Navigator): CompanyIsRegisteredNameController =
    new CompanyIsRegisteredNameController(
      appConfig = base.frontendAppConfig,
      messagesApi = base.messagesApi,
      cacheConnector = connector,
      navigator = nav,
      authenticate = FakeAuthAction,
      allowAccess = FakeAllowAccessProvider(),
      getData = dataRetrievalAction,
      requireData = new DataRequiredActionImpl(),
      formProvider = new IsRegisteredNameFormProvider()
    )

}
