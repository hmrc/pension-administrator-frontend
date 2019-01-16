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

package controllers

import base.SpecBase
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{BusinessDetails, PSAUser, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.BusinessDetailsViewModel
import views.html.businessDetails
import scala.concurrent.Future

trait BusinessDetailsControllerBehaviour {
  this: ControllerSpecBase =>

  import BusinessDetailsControllerBehaviour._

  // scalastyle:off method.length

  def businessDetailsController[I <: TypedIdentifier[BusinessDetails]](
                                                                        testFormModel: BusinessDetailsFormModel,
                                                                        testViewModel: BusinessDetailsViewModel,
                                                                        id: I,
                                                                        createController: (UserAnswersCacheConnector, Navigator) => BusinessDetailsController
                                                                      ): Unit = {

    "return OK and the correct view for a GET request" in {
      val fixture = testFixture(createController, testFormModel, testViewModel)

      val result = Future(fixture.controller.get(id)(testRequest()))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(fixture.form, testViewModel, this)
    }

    "populate the view correctly for a GET request with existing data" in {
      val answers = UserAnswers().set(id)(testBusinessDetails).asOpt.value
      val fixture = testFixture(createController, testFormModel, testViewModel)

      val result = Future(fixture.controller.get(id)(testRequest(answers)))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(fixture.form.fill(testBusinessDetails), testViewModel, this)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(createController, testFormModel, testViewModel)
      val request = testRequest(businessDetails = Some(testBusinessDetails))

      val result = fixture.controller.post(id)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the user answer when valid data is submitted" in {
      val fixture = testFixture(createController, testFormModel, testViewModel)

      fixture.controller.post(id)(testRequest(businessDetails = Some(testBusinessDetails)))

      fixture.dataCacheConnector.verify(id, testBusinessDetails)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(createController, testFormModel, testViewModel)
      val request = testRequest(businessDetails = Some(invalidBusinessDetails))
      val formWithErrors = fixture.form.bindFromRequest()(request)

      val result = fixture.controller.post(id)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors, testViewModel, this)
    }

  }

  // scalastyle:off method.length

}

//scalastyle:off magic.number

object BusinessDetailsControllerBehaviour {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val testBusinessDetails = BusinessDetails("test company name", Some("1234567890"))
  val invalidBusinessDetails = BusinessDetails("", Some(""))

  case class TestFixture(dataCacheConnector: FakeUserAnswersCacheConnector, controller: BusinessDetailsController, form: Form[BusinessDetails])

  def testFixture(
                   createController: (UserAnswersCacheConnector, Navigator) => BusinessDetailsController,
                   testFormModel: BusinessDetailsFormModel,
                   testViewModel: BusinessDetailsViewModel
                 ): TestFixture = {
    val connector = new FakeUserAnswersCacheConnector() {}
    val navigator = new FakeNavigator(onwardRoute)

    val controller = createController(connector, navigator)

    val form = new BusinessDetailsFormProvider(isUK=true).apply(testFormModel)

    TestFixture(connector, controller, form)
  }

  def testRequest(answers: UserAnswers = UserAnswers(), businessDetails: Option[BusinessDetails] = None): DataRequest[AnyContent] = {
    val fakeRequest = FakeRequest("", "")

    val request = businessDetails map {
      details =>
        fakeRequest.withFormUrlEncodedBody(
          ("companyName", details.companyName),
          ("utr", details.uniqueTaxReferenceNumber.get)
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(UserType.Individual, "userId", None, false, None),
      userAnswers = answers
    )
  }

  def viewAsString(form: Form[_], testViewModel: BusinessDetailsViewModel, base: SpecBase): String = {
    businessDetails(
      base.frontendAppConfig,
      form,
      testViewModel
    )(
      base.fakeRequest,
      base.messages
    ).toString
  }

}
