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

import forms.behaviours.FormBehaviours
import forms.mappings.Constraints
import models.{Address, Field, Required}
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.FormError

import scala.util.Random

class AddressFormProviderSpec extends FormBehaviours with FormSpec{

  val addressLineMaxLength = 35

  def alphaString(max: Int = addressLineMaxLength) = Random.alphanumeric take Random.nextInt(max) mkString ""

  val addressLine1 = alphaString()
  val addressLine2 = alphaString()
  val addressLine3 = alphaString()
  val addressLine4 = alphaString()
  val postCode = "ZZ1 1ZZ"

  val validData: Map[String, String] = Map(
    "addressLine1" -> addressLine1,
    "addressLine2" -> addressLine2,
    "addressLine3" -> addressLine3,
    "addressLine4" -> addressLine4,
    "postCode.postCode" -> postCode,
    "country" -> "GB"
  )

  val form = new AddressFormProvider()()

  "Address form" must {
    behave like questionForm(Address(
      addressLine1,
      addressLine2,
      Some(addressLine3),
      Some(addressLine4),
      Some(postCode),
      "GB"
    ))

    behave like formWithMandatoryTextFields(
      Field("addressLine1", Required -> "error.address_line_1.required"),
      Field("addressLine2", Required -> "error.address_line_2.required"),
      Field("country", Required -> "error.country.required"
      )
    )

    "successfully bind" when {
      "country is not UK and postcode is any postcode" in {

        val postCode = alphaString(6)

        val data = validData + ("postCode.postCode" -> postCode, "country" -> "AF")
        val result = form.bind(data)
        result.get shouldEqual Address(
          addressLine1,
          addressLine2,
          Some(addressLine3),
          Some(addressLine4),
          Some(postCode),
          "AF"
        )
      }
      "country is UK and postcode is a valid postcode" in {
        val data = validData + ("postCode.postCode" -> postCode, "country" -> "GB")
        val result = form.bind(data)
        result.get shouldEqual Address(
          addressLine1,
          addressLine2,
          Some(addressLine3),
          Some(addressLine4),
          Some(postCode),
          "GB"
        )
      }
    }

    "fail to bind" when {
      s"address line 1 exceeds max length $addressLineMaxLength" in {
        val addressLine1 = RandomStringUtils.randomAlphabetic(addressLineMaxLength + 1)
        val data = validData + ("addressLine1" -> addressLine1)

        val expectedError: Seq[FormError] = error("addressLine1", "error.address_line_1.length", addressLineMaxLength)
        checkForError(form, data, expectedError)
      }

      s"address line 2 exceeds max length $addressLineMaxLength" in {
        val addressLine2 = RandomStringUtils.randomAlphabetic(addressLineMaxLength + 1)
        val data = validData + ("addressLine2" -> addressLine2)

        val expectedError: Seq[FormError] = error("addressLine2", "error.address_line_2.length", addressLineMaxLength)
        checkForError(form, data, expectedError)
      }

      s"address line 3 exceeds max length $addressLineMaxLength" in {
        val addressLine3 = RandomStringUtils.randomAlphabetic(addressLineMaxLength + 1)
        val data = validData + ("addressLine3" -> addressLine3)

        val expectedError: Seq[FormError] = error("addressLine3", "error.address_line_3.length", addressLineMaxLength)
        checkForError(form, data, expectedError)
      }

      s"address line 4 exceeds max length $addressLineMaxLength" in {
        val addressLine4 = RandomStringUtils.randomAlphabetic(addressLineMaxLength + 1)
        val data = validData + ("addressLine4" -> addressLine4)

        val expectedError: Seq[FormError] = error("addressLine4", "error.address_line_4.length", addressLineMaxLength)
        checkForError(form, data, expectedError)
      }

      "postcode is missing for country UK" in {
        val validData: Map[String, String] = Map(
          "addressLine1" -> addressLine1,
          "addressLine2" -> addressLine2,
          "addressLine3" -> addressLine3,
          "addressLine4" -> addressLine4,
          "country" -> "GB"
        )
        val expectedError: Seq[FormError] = error("postCode.postCode", "error.postcode.required")
        checkForError(form, validData, expectedError)
      }
    }
  }
}