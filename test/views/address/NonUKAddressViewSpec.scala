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

package views.address

import forms.address.NonUKAddressFormProvider
import models.Address
import play.api.data.Form
import play.api.mvc.Call
import utils.{FakeCountryOptions, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.behaviours.QuestionViewBehaviours
import views.html.address.nonukAddress

class NonUKAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "companyRegisteredNonUKAddress"
  val countryOptions: Seq[InputOption] = Seq(InputOption("AF", "Afghanistan"), InputOption("territory:AE-AZ", "Abu Dhabi"))
  val companyName: String = "Test Company Name"

  val viewModel = ManualAddressViewModel(
    Call("GET", "/"),
    countryOptions,
    Message("companyRegisteredNonUKAddress.title"),
    Message("companyRegisteredNonUKAddress.heading", companyName),
    Some(Message("companyRegisteredNonUKAddress.hintText"))
  )

  override val form = new NonUKAddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))()

  val view: nonukAddress = app.injector.instanceOf[nonukAddress]

  def createView: () => _root_.play.twirl.api.HtmlFormat.Appendable = () =>
    view(new NonUKAddressFormProvider(
      new FakeCountryOptions(environment, frontendAppConfig)).apply(), viewModel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => _root_.play.twirl.api.HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel)(fakeRequest, messages)

  "ManualAddress view" must {

    behave like normalPageWithDynamicTitle(
      createView, messageKeyPrefix, Message("companyRegisteredNonUKAddress.heading", companyName),  "hintText")

    behave like pageWithTextFields(
        createViewUsingForm,
        messageKeyPrefix,
        controllers.register.company.routes.CompanyRegisteredAddressController.onSubmit().url,
    "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )

    behave like pageWithSubmitButton(createView)

  }
  app.stop()
}
