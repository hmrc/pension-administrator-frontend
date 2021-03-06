/*
 * Copyright 2021 HM Revenue & Customs
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

package views.register.company

import forms.register.company.CompanyAddressFormProvider
import models.TolerantAddress
import play.api.data.Form
import utils.countryOptions.CountryOptions
import views.behaviours.{AddressBehaviours, ViewBehaviours, YesNoViewBehaviours}
import views.html.register.company.confirmCompanyDetails

class ConfirmCompanyDetailsViewSpec extends ViewBehaviours with AddressBehaviours with YesNoViewBehaviours {

  private val messageKeyPrefix = "confirmCompanyAddress"

  private val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )
private val company = "the company"
  val formProvider = new CompanyAddressFormProvider

  val form: Form[Boolean] = formProvider()

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  val view: confirmCompanyDetails = app.injector.instanceOf[confirmCompanyDetails]


  private def createView(address: TolerantAddress = testAddress) =
    () => view(
      form,
      address,
      company,
      countryOptions
    )(fakeRequest, messages)

  private def createViewUsingForm =
    (form: Form[_]) => view(
      form,
      testAddress,
      company,
      countryOptions
    )(fakeRequest, messages)

  "CompanyAddress view" must {
    behave like normalPageWithDynamicTitle(
      view = createView(),
      messageKeyPrefix = messageKeyPrefix,
      dynamicContent =  company
    )

    behave like pageWithAddress(address => createView(address)(), "companyAddress")

    behave like pageWithSubmitButton(createView())

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix,
      controllers.register.company.routes.ConfirmCompanyDetailsController.onSubmit().url, s"$messageKeyPrefix.title")
  }

}
