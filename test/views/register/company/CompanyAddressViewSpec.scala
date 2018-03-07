/*
 * Copyright 2018 HM Revenue & Customs
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

import models.TolerantAddress
import views.behaviours.{AddressBehaviours, ViewBehaviours}
import views.html.register.company.companyAddress

class CompanyAddressViewSpec extends ViewBehaviours with AddressBehaviours {

  private val messageKeyPrefix = "companyAddress"

  private val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  private def createView(address: TolerantAddress = testAddress) = () => companyAddress(frontendAppConfig, address)(fakeRequest, messages)

  "CompanyAddress view" must {
    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), messages("site.secondaryHeader") )

    behave like pageWithAddress((address) => createView(address)(), "companyAddress")

    behave like pageWithSubmitButton(createView())
  }

}
