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

package controllers.address

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.FakeAllowAccessProvider
import forms.address.AddressListFormProvider
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import org.scalatest.{Matchers, WordSpec}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{Call, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class AddressListControllerSpec extends WordSpec with Matchers {

  import AddressListControllerSpec._

  "get" must {

    "return Ok and the correct view when no addresses" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel(Nil)
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) shouldBe OK
        contentAsString(result) shouldBe viewAsString(viewModel, None)
      }

    }

    "return Ok and the correct view when addresses are supplied" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onPageLoad(viewModel)

        status(result) shouldBe OK
        contentAsString(result) shouldBe viewAsString(viewModel, None)
      }

    }

  }

  "post" must {

    "return See Other on submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        status(result) shouldBe SEE_OTHER
      }

    }

    "redirect to the page specified by the navigator following submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(addressListViewModel(), 0)

        redirectLocation(result) shouldBe Some(onwardRoute.url)
      }

    }

    "save the user answer on submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) shouldBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(fakeAddressListId, viewModel.addresses.head)
      }

    }

    "delete any existing address on submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, 0)

        status(result) shouldBe SEE_OTHER
        FakeUserAnswersCacheConnector.verifyNot(fakeAddressId)
      }

    }

    "return Bad Request and the correct view on submission of invalid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val result = controller.onSubmit(viewModel, -1)

        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe viewAsString(viewModel, Some(-1))
      }

    }

  }

}

object AddressListControllerSpec extends SpecBase {

  val view: addressList = app.injector.instanceOf[addressList]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  val view: addressList
                                )(implicit val executionContext: ExecutionContext) extends AddressListController {

    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

    override protected def navigator: Navigator = new FakeNavigator(onwardRoute)

    override val allowAccess = FakeAllowAccessProvider()

    def onPageLoad(viewModel: AddressListViewModel): Future[Result] = {

      get(
        viewModel,
        NormalMode
      )(DataRequest(FakeRequest(), "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers()))

    }

    def onSubmit(viewModel: AddressListViewModel, value: Int): Future[Result] = {

      val request = FakeRequest().withFormUrlEncodedBody("value" -> value.toString)

      post(
        viewModel,
        fakeAddressListId,
        fakeAddressId,
        NormalMode
      )(DataRequest(request, "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers()))

    }

    override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()

  }

  val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val fakeAddressListId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress]() {}
  val fakeAddressId: TypedIdentifier[Address] = new TypedIdentifier[Address]() {}

  private lazy val postCall = controllers.routes.IndexController.onPageLoad()
  private lazy val manualInputCall = controllers.routes.SessionExpiredController.onPageLoad()

  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  def addressListViewModel(addresses: Seq[TolerantAddress] = addresses): AddressListViewModel =
    AddressListViewModel(
      postCall,
      manualInputCall,
      addresses,
      Message("title text"),
      Message("heading text"),
      Message("select an address text"),
      Message("select an address link text")
    )

  def viewAsString(viewModel: AddressListViewModel, value: Option[Int]): String = {

    val request = FakeRequest()
    val messages = app.injector.instanceOf[MessagesApi].preferred(request)

    val form = value match {
      case Some(i) => new AddressListFormProvider()(viewModel.addresses).bind(Map("value" -> i.toString))
      case None => new AddressListFormProvider()(viewModel.addresses)
    }

    view(form, viewModel, NormalMode)(request, messages).toString()

  }

}
