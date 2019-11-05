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

package views.register.partnership

import controllers.register.partnership.routes
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import models.TolerantAddress
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.countryOptions.CountryOptions
import views.behaviours.{AddressBehaviours, ViewBehaviours, YesNoViewBehaviours}
import views.html.register.partnership.confirmPartnershipDetails

class ConfirmPartnershipDetailsViewSpec extends ViewBehaviours with AddressBehaviours with YesNoViewBehaviours {

  val messageKeyPrefix = "confirmRegisteredAddress"

  private val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  val formProvider = new ConfirmPartnershipDetailsFormProvider

  val form: Form[Boolean] = formProvider()

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def createView(address: TolerantAddress = testAddress): () => HtmlFormat.Appendable =
    () => confirmPartnershipDetails(frontendAppConfig, form, "Partnership", address, countryOptions)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmPartnershipDetails(
      frontendAppConfig,
      form,
      "",
      testAddress,
      countryOptions
    )(fakeRequest, messages)

  "ConfirmPartnershipDetails view" must {

    behave like normalPageWithDynamicTitle(createView(), messageKeyPrefix, "Partnership")

    behave like pageWithAddress(address => createView(address)(), "partnershipAddress")

    behave like pageWithSubmitButton(createView())

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.ConfirmPartnershipDetailsController.onSubmit().url, s"$messageKeyPrefix.title")
  }

}
