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

import forms.address.SameContactAddressFormProvider
import models.{Mode, NormalMode, TolerantAddress, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.address.sameContactAddress

class SameContactAddressViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "individual.same.contact.address"

  val formProvider = new SameContactAddressFormProvider()
  val form = formProvider("error.required")

  val testAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some("test post code"),
    Some("GB")
  )

  val testCountry = "United Kingdom"

  def viewmodel(mode: Mode = NormalMode) = SameContactAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = Message("individual.same.contact.address.title"),
    heading = Message("individual.same.contact.address.heading"),
    hint = Some(Message("individual.same.contact.address.hint")),
    address = testAddress,
    psaName = "Test name",
    mode = mode,
    displayReturnLink = true
  )

  val view: sameContactAddress = app.injector.instanceOf[sameContactAddress]

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def createView: () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(), countryOptions)(fakeRequest, messages)

  def createViewUpdateMode: () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(UpdateMode), countryOptions)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel(), countryOptions)(fakeRequest, messages)

  "Same Contact Address View" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, "www.example.com", s"$messageKeyPrefix.heading")

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createViewUpdateMode, controllers.routes.PsaDetailsController.onPageLoad().url)

    "display the address" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "address-value-0", testAddress.addressLine1.value)
      assertRenderedByIdWithText(doc, "address-value-1", testAddress.addressLine2.value)
      assertRenderedByIdWithText(doc, "address-value-2", testAddress.addressLine3.value)
      assertRenderedByIdWithText(doc, "address-value-3", testAddress.addressLine4.value)
      assertRenderedByIdWithText(doc, "address-value-4", testAddress.postcode.value)
      assertRenderedByIdWithText(doc, "address-value-5", testCountry)
    }
  }

}
