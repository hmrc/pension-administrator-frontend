/*
 * Copyright 2023 HM Revenue & Customs
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

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import utils.Enumerable
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate

object MappingsSpec {

  sealed trait Foo

  case object Bar extends Foo

  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v)*)
  }

}

// scalastyle:off magic.number

class MappingsSpec extends AnyWordSpecLike with Matchers with OptionValues with Mappings with ScalaCheckDrivenPropertyChecks with Generators {

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

    "trim spaces from a valid string" in {
      val gen = RegexpGen.from("""^\s+test\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe "test"
      }
    }

    "minimise internal spaces" in {
      val gen = RegexpGen.from("""^start\s+middle\s+end$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe "start middle end"
      }
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind a string containing only spaces" in {
      val gen = RegexpGen.from("""^\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.errors must contain(FormError("value", "error.required"))
      }
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

  "optionalText" must {

    val testForm: Form[Option[String]] =
      Form(
        "value" -> optionalText()
      )

    "bind a valid string" in {
      forAll(stringsLongerThan(0)) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some(s)
      }
    }

    "trim spaces from a valid string" in {
      val gen = RegexpGen.from("""^\s+test\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some("test")
      }
    }

    "minimise internal spaces" in {
      val gen = RegexpGen.from("""^start\s+middle\s+end$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe Some("start middle end")
      }
    }

    "bind an empty string to None" in {
      val result = testForm.bind(Map("value" -> ""))
      result.get mustBe None
    }

    "bind a string containing only spaces to None" in {
      val gen = RegexpGen.from("""^\s+$""")
      forAll(gen) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.get mustBe None
      }
    }

    "bind an empty map to None" in {
      val result = testForm.bind(Map.empty[String, String])
      result.get mustBe None
    }

    "unbind some valid value" in {
      forAll(Gen.alphaStr) { s =>
        val result = testForm.fill(Some(s))
        result.apply("value").value mustBe Some(s)
      }
    }

    "unbind None to an empty string" in {
      val result = testForm.fill(None)
      result.apply("value").value mustBe Some("")
    }

  }

  "optionalText with constraint" must {

    val testForm: Form[Option[String]] =
      Form(
        "value" -> optionalText()
          .verifying(maxLength(5, "error.length"))
      )

    "not enforce the constraint for an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.get mustBe None
    }

    "not enforce the constraint for an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.get mustBe None
    }

    "enforce the constraint for a valid string" in {
      forAll(stringsLongerThan(5)) { s =>
        val result = testForm.bind(Map("value" -> s))
        result.errors mustBe Seq(FormError("value", Seq("error.length"), Seq(5)))
      }
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

    // scalastyle:off magic.number
    "unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
    // scalastyle:on magic.number
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

  "date" must {
    case class TestClass(date: LocalDate)

    val testForm: Form[TestClass] = Form(
      mapping(
        "date" -> date("error.required", "error.invalid")
      )(TestClass.apply)(o => Some(o.date))
    )

    // scalastyle:off magic.number
    val testDate = LocalDate.of(1862, 6, 9)
    // scalastyle:on magic.number

    "bind valid data" in {
      val result = testForm.bind(
        Map(
          "date.day" -> testDate.getDayOfMonth.toString,
          "date.month" -> testDate.getMonthValue.toString,
          "date.year" -> testDate.getYear.toString
        )
      )

      result.errors.size mustBe 0
      result.get.date mustBe testDate
    }

    "not bind blank data" in {
      val result = testForm.bind(
        Map(
          "date.day" -> "",
          "date.month" -> "",
          "date.year" -> ""
        )
      )

      result.errors.size mustBe 3
      result.errors must contain.allOf(
        FormError("date.day", "error.date.day_blank"),
        FormError("date.month", "error.date.month_blank"),
        FormError("date.year", "error.date.year_blank")
      )
    }

    "not bind non-numeric input" in {
      val result = testForm.bind(
        Map(
          "date.day" -> "A",
          "date.month" -> "B",
          "date.year" -> "C"
        )
      )

      result.errors.size mustBe 3
      result.errors must contain.allOf(
        FormError("date.day", "error.date.day_invalid"),
        FormError("date.month", "error.date.month_invalid"),
        FormError("date.year", "error.date.year_invalid")
      )
    }

    "not bind invalid day" in {
      val result = testForm.bind(
        Map(
          "date.day" -> "0",
          "date.month" -> testDate.getMonthValue.toString,
          "date.year" -> testDate.getYear.toString
        )
      )

      result.errors.size.mustBe(1)
      result.errors must contain(FormError("date", "error.invalid"))
    }

    "not bind invalid month" in {
      val result = testForm.bind(
        Map(
          "date.day" -> testDate.getDayOfMonth.toString,
          "date.month" -> "0",
          "date.year" -> testDate.getYear.toString
        )
      )

      result.errors.size.mustBe(1)
      result.errors must contain(FormError("date", "error.invalid"))
    }

    "fill correctly from model" in {
      val testClass = TestClass(testDate)
      val result = testForm.fill(testClass)

      result("date.day").value.value mustBe "9"
      result("date.month").value.value mustBe "6"
      result("date.year").value.value mustBe "1862"
    }
  }

}
