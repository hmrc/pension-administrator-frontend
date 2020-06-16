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

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.company.routes.CompanyAddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.{BusinessNameId, BusinessUTRId}
import models.{AddressYears, NormalMode}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.MessagesControllerComponents
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class CompanyAddressYearsControllerSpec extends ControllerSpecBase {

  import CompanyAddressYearsControllerSpec._

  "render the view correctly on a GET request" in {
    val request = addCSRFToken(FakeRequest(CompanyAddressYearsController.onPageLoad(NormalMode)))
    val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messages).toString
  }

  "redirect to the next page on a POST request" in {
    running(_.overrides(modules(dataRetrieval)++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(FakeNavigator),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
      ):_*)) {
      app =>
        val controller = app.injector.instanceOf[CompanyAddressYearsController]

        val request = FakeRequest().withFormUrlEncodedBody("value" -> AddressYears.OverAYear.toString)

        val result = controller.onSubmit(NormalMode)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
    }
  }
}
object CompanyAddressYearsControllerSpec extends CompanyAddressYearsControllerSpec {

  val companyName = "Test Company Name"

  val view: addressYears = app.injector.instanceOf[addressYears]

  val dataRetrieval: DataRetrievalAction = UserAnswers()
    .set(BusinessNameId)(companyName).flatMap(_.set(BusinessUTRId)("Test UTR")).asOpt.value
    .dataRetrievalAction

  val viewModel = AddressYearsViewModel(
    CompanyAddressYearsController.onSubmit(NormalMode),
    title = Message("addressYears.heading", Message("theCompany")),
    heading = Message("addressYears.heading", companyName),
    legend = Message("addressYears.heading", companyName)
  )

  val form = new AddressYearsFormProvider()(companyName)

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind[MessagesControllerComponents].to(stubMessagesControllerComponents())
    ).build()

}

