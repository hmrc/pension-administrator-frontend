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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class AddressListControllerSpec extends Matchers with AnyWordSpecLike {

  import AddressListControllerSpec._

  "get" must {

    "return Ok and the correct view when no addresses" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel(Nil)
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onPageLoad(viewModel, form(Nil, "error.required"))

        status(result) shouldBe OK
        contentAsString(result) shouldBe viewAsString(viewModel, None)
      }

    }

    "return Ok and the correct view when addresses are supplied" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onPageLoad(viewModel, form(addresses, "error.required"))

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
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(addressListViewModel(), 0, form(addresses, "error.required"), addresses)

        status(result) shouldBe SEE_OTHER
      }

    }

    "redirect to the page specified by the navigator following submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(addressListViewModel(), 0, form(addresses, "error.required"), addresses)

        redirectLocation(result) shouldBe Some(onwardRoute.url)
      }

    }

    "save the user answer on submission of valid data when address is incomplete and redirect to manualInput page" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(incompleteAddresses)
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(viewModel, 0, form(incompleteAddresses, "error.required"), incompleteAddresses)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(manualInputCall.url)
      }

    }

    "shuffle the address lines and save fixed address when address is incomplete but fixable" in {

      running(_.overrides()) { app =>
        val viewModel = addressListViewModel(fixableAddress)
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(viewModel, 0, form(fixableAddress, "error.required"), fixableAddress)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(postCall.url)
      }

    }

    "delete any existing address on submission of valid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(viewModel, 0, form(addresses, "error.required"), addresses)

        status(result) shouldBe SEE_OTHER
      }

    }

    "return Bad Request and the correct view on submission of invalid data" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) { app =>
        val viewModel = addressListViewModel()
        val controller = app.injector.instanceOf[TestController]
        val form = app.injector.instanceOf[AddressListFormProvider]
        val result = controller.onSubmit(viewModel, -1, form(addresses, "error.required"), addresses)

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

    override val allowAccess = FakeAllowAccessProvider(config = frontendAppConfig)

    def onPageLoad(viewModel: AddressListViewModel, form: Form[Int]): Future[Result] = {

      get(
        viewModel,
        NormalMode,
        form
      )(DataRequest(FakeRequest(), "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers()))

    }

    def onSubmit(viewModel: AddressListViewModel, value: Int, form: Form[Int], addressSeq: Seq[TolerantAddress] = addresses): Future[Result] = {

      val request = FakeRequest().withFormUrlEncodedBody("value" -> value.toString)
      val fakeSeqTolerantAddressId: TypedIdentifier[Seq[TolerantAddress]] = new TypedIdentifier[Seq[TolerantAddress]] {
        override def toString = "abc"
      }
      val json = Json.obj(
        fakeSeqTolerantAddressId.toString -> addressSeq
      )
      post(
        viewModel,
        fakeAddressId,
        fakeAddressListId,
        fakeSeqTolerantAddressId,
        NormalMode,
        form
      )(DataRequest(request, "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers(json)))

    }

    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents

  }

  val onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val fakeAddressListId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress]() {}
  val fakeAddressId: TypedIdentifier[Address] = new TypedIdentifier[Address]() {}

  private lazy val postCall = controllers.routes.IndexController.onPageLoad
  private lazy val manualInputCall = controllers.routes.SessionExpiredController.onPageLoad

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

  private val incompleteAddresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      None, None, None,
      Some("A1 1PC"),
      Some("GB")
    ))

  private val fixableAddress = Seq(
    TolerantAddress(
      Some("Address 2 Line 1"),
      None,
      None,
      Some("Address 2 Line 4"),
      Some("123"),
      Some("GB")
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
      case Some(i) => new AddressListFormProvider()(viewModel.addresses, "error.required").bind(Map("value" -> i.toString))
      case None => new AddressListFormProvider()(viewModel.addresses, "error.required")
    }

    view(form, viewModel, NormalMode)(request, messages).toString()

  }

}
