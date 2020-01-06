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

package views.address

import controllers.register.individual.routes
import forms.AddressFormProvider
import models.{Address, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import utils.{FakeCountryOptions, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.behaviours.QuestionViewBehaviours
import views.html.address.manualAddress

class ManualAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "common.manual.address"
  val countryOptions: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val schemeName: String = "Test Scheme Name"

  val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions,
    Message("common.manual.address.title"),
    Message("common.manual.address.heading"),
    psaName = Some("test psa name")
  )

  override val form = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))()

  val view: manualAddress = app.injector.instanceOf[manualAddress]

  def createView(mode: Mode = NormalMode): () => _root_.play.twirl.api.HtmlFormat.Appendable = () =>
    view(new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig)).apply(),
      viewModel, mode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel, UpdateMode)(fakeRequest, messages)

  "ManualAddress view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      routes.IndividualPreviousAddressController.onSubmit(NormalMode).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )
  }

}
