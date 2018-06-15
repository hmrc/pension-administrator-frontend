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

package forms

import forms.behaviours.{AddressBehaviours, FormBehaviours}
import forms.mappings.AddressMapping
import models.Address
import utils.FakeCountryOptions

import scala.util.Random

class AddressFormProviderSpec extends FormBehaviours with FormSpec with AddressBehaviours {

  private def alphaString(max: Int = AddressMapping.maxAddressLineLength) =
    Random.alphanumeric take Random.shuffle(Range(1, max).toList).head mkString ""

  private val addressLine1 = alphaString()
  private val addressLine2 = alphaString()
  private val addressLine3 = alphaString()
  private val addressLine4 = alphaString()
  private val postCode = "ZZ1 1ZZ"

  private val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val validData: Map[String, String] = Map(
    "addressLine1" -> addressLine1,
    "addressLine2" -> addressLine2,
    "addressLine3" -> addressLine3,
    "addressLine4" -> addressLine4,
    "postCode" -> postCode,
    "country" -> "GB"
  )

  val form = new AddressFormProvider(countryOptions)()

  "Address form" must {
    behave like questionForm(Address(
      addressLine1,
      addressLine2,
      Some(addressLine3),
      Some(addressLine4),
      Some(postCode),
      "GB"
    ))

    behave like formWithCountry(
      form,
      "country",
      "error.country.invalid",
      "error.country.invalid",
      countryOptions,
      Map(
        "addressLine1" -> addressLine1,
        "addressLine2" -> addressLine2
      )
    )

    behave like formWithCountryAndPostCode(
      form,
      "error.postcode.required",
      "error.postcode.invalid",
      "error.postcode.nonUK.length",
      Map(
        "addressLine1" -> addressLine1,
        "addressLine2" -> addressLine2
      ),
      (address: Address) => address.postcode.getOrElse("")
    )

    "behave like a form with address lines" when {

      behave like formWithAddressField(
        form,
        "addressLine1",
        "error.address_line_1.required",
        "error.address_line_1.length",
        "error.address_line_1.invalid"
      )

      behave like formWithAddressField(
        form,
        "addressLine2",
        "error.address_line_2.required",
        "error.address_line_2.length",
        "error.address_line_2.invalid"
      )

      behave like formWithOptionalAddressField(
        form,
        "addressLine3",
        "error.address_line_3.length",
        "error.address_line_3.invalid"
      )

      behave like formWithOptionalAddressField(
        form,
        "addressLine4",
        "error.address_line_4.length",
        "error.address_line_4.invalid"
      )

    }

  }

}
