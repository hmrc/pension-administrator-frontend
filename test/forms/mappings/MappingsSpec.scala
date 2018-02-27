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

package forms.mappings

import models.register.company.DirectorNino
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.{Form, FormError}
import utils.Enumerable

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends WordSpec with MustMatchers with OptionValues with Mappings {

  import MappingsSpec._

  "text" must {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "return a custom error message" in {
      val form = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" must {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" must {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" must {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "postCode" must {

    val testForm = Form("postCode" -> postCode("error.required", "error.invalid"))

    "bind successfully when country is non UK and postcode is not of UK postal format" in {
      val result = testForm.bind(Map("country" -> "IN", "postCode.postCode" -> "", "postCode.postCode" -> "sdsad"))
      result.get mustEqual Some("sdsad")
    }

    "bind successfully when country is UK and postcode is of correct format" in {
      val result = testForm.bind(Map("country" -> "GB", "postCode.postCode" -> "", "postCode.postCode" -> "AB1 1AB"))
      result.get mustEqual Some("AB1 1AB")
    }

    "fail to bind when postCode is not provided" in {
      val result = testForm.bind(Map("country" -> "GB", "postCode.postCode" -> "", "postCode.postCode" -> ""))

      result.errors mustEqual Seq(FormError("postCode.postCode", "error.required"))
    }

    Seq("A 1223", "1234 A23", "AA1 BBB", "AA 8989").foreach{ testPostCode =>
      s"fail to bind when postCode $testPostCode is not valid" in {

        val result = testForm.bind(Map("country" -> "GB", "postCode.postCode" -> "", "postCode.postCode" -> testPostCode))

        result.errors mustEqual Seq(FormError("postCode.postCode", "error.invalid", Seq(postcode)))

      }
    }
  }

  "directorNino" must {

    val testForm: Form[DirectorNino] = Form("directorNino" ->  directorNinoMapping())

    "fail to bind when yes is selected but NINO is not provided" in {
      val result = testForm.bind(Map("directorNino.hasNino" -> "true"))
      result.errors mustEqual Seq(FormError("directorNino.nino", "common.error.nino.required"))
    }

    "fail to bind when no is selected but reason is not provided" in {
      val result = testForm.bind(Map("directorNino.hasNino" -> "false"))
      result.errors mustEqual Seq(FormError("directorNino.reason", "directorNino.error.reason.required"))
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val result = testForm.bind(Map("directorNino.hasNino" -> "true", "directorNino.nino" -> nino))
        result.errors mustEqual Seq(FormError("directorNino.nino", "common.error.nino.invalid"))
      }
    }

    "fail to bind when no is selected and reason exceeds max length of 150" in {
      val testString = RandomStringUtils.randomAlphabetic(151)
      val result = testForm.bind(Map("directorNino.hasNino" -> "false", "directorNino.reason" -> testString))
      result.errors mustEqual Seq(FormError("directorNino.reason", "directorNino.error.reason.length", Seq(150)))
    }
  }
}
