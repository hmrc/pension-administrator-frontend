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

package views.behaviours

import models.TolerantAddress
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait AddressBehaviours {
  this: ViewSpecBase =>

  def pageWithAddress(createView: TolerantAddress => HtmlFormat.Appendable, fieldName: String): Unit = {

    val countryName = "United Kingdom"

    "display an address correctly" when {
      "rendering a full address" in {
        val address = TolerantAddress(
          Some("Some Building"),
          Some("1 Some Street"),
          Some("Some Village"),
          Some("Some Town"),
          Some("ZZ1 1ZZ"),
          Some("GB")
        )

        val doc = asDocument(createView(address))
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine1", address.addressLine1.value)
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine2", address.addressLine2.value)
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine3", address.addressLine3.value)
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine4", address.addressLine4.value)
        assertRenderedByIdWithText(doc, s"$fieldName-postcode", address.postcode.value)
        assertRenderedByIdWithText(doc, s"$fieldName-country", countryName)
      }

      "rendering a minimal address" in {
        val address = TolerantAddress(
          Some("Some Building"),
          Some("1 Some Street"),
          None,
          None,
          None,
          Some("GB")
        )

        val doc = asDocument(createView(address))
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine1", address.addressLine1.value)
        assertRenderedByIdWithText(doc, s"$fieldName-addressLine2", address.addressLine2.value)
        assertRenderedByIdWithText(doc, s"$fieldName-country", countryName)

        assertNotRenderedById(doc, s"$fieldName-addressLine3")
        assertNotRenderedById(doc, s"$fieldName-addressLine4")
        assertNotRenderedById(doc, s"$fieldName-postcode")
      }
    }

  }

}
