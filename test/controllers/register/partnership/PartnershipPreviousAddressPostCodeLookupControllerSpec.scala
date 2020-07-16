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

import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models._
import models.requests.DataRequest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}
import play.api.test.CSRFTokenHelper.addCSRFToken

class PartnershipPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers())
  implicit val request: DataRequest[AnyContent] =
    DataRequest(fakeRequest, "", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None), UserAnswers())
  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()

  private val validPostcode = "ZZ1 1ZZ"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  private val address = TolerantAddress(
    Some("test-address-line-1"),
    Some("test-address-line-2"),
    None,
    None,
    Some(validPostcode),
    Some("GB")
  )

  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }

  private val onwardRoute = controllers.routes.SessionExpiredController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)


  "PartnershipPreviousAddressPostCodeLookupController" must {

    "render the view correctly on a GET request" in {
      val request = addCSRFToken(FakeRequest(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)))
      val result = route(application, request).value
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messagesApi.preferred(fakeRequest)).toString()

    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getPartnership)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[PartnershipPreviousAddressPostCodeLookupController]

          val request = FakeRequest().withFormUrlEncodedBody("value" -> validPostcode)

          val result = controller.onSubmit(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

  }

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
      bind[DataRetrievalAction].toInstance(getPartnership),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).to(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

  def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) = PostcodeLookupViewModel(
    routes.PartnershipPreviousAddressPostCodeLookupController.onSubmit(mode),
    routes.PartnershipPreviousAddressController.onPageLoad(mode),
    Message("previous.postcode.lookup.heading", Message("thePartnership")),
    Message("previous.postcode.lookup.heading", "Test Partnership Name"),
    Message("manual.entry.text"),
    Some(Message("manual.entry.link")),
    Message("postcode.lookup.form.label"),
    None
  )
}


