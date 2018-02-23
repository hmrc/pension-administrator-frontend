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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{MustMatchers, WordSpec}

class TransformsSpec extends WordSpec with MustMatchers with TableDrivenPropertyChecks with Transforms {

  "vatRegistrationNumberTransform" must {
    "strip leading, trailing ,and internal spaces" in {
      val actual = vatRegistrationNumberTransform("  123 456 789  ")
      actual mustBe "123456789"
    }

    "remove leading GB" in {
      val gb = Table(
        "vat",
        "GB123456789",
        "Gb123456789",
        "gB123456789",
        "gb123456789"
      )

      forAll(gb) {vat =>
        vatRegistrationNumberTransform(vat) mustBe "123456789"
      }
    }
  }

  "postCodeTransform" must {
    "strip leading and trailing spaces" in {
      val actual = postCodeTransform(" AB12 1AB ")
      actual mustBe "AB12 1AB"
    }

    "upper case all characters" in {
      val actual = postCodeTransform("ab12 1ab")
      actual mustBe "AB12 1AB"
    }

    "minimise spaces" in {
      val actual = postCodeTransform("AB12     1AB")
      actual mustBe "AB12 1AB"
    }
  }

  "postCodeValidTransform" must {
    "add missing internal space in full post code" in {
      val actual = postCodeValidTransform("AB121AB")
      actual mustBe "AB12 1AB"
    }

    "add missing internal space in minimal post code" in {
      val actual = postCodeValidTransform("A11AB")
      actual mustBe "A1 1AB"
    }
  }

  "noTransform" must {
    "leave its input unchanged" in {
      val expected = " a B c "
      val actual = noTransform(expected)
      actual mustBe expected
    }
  }

  "standard text transform" must {
    "strip leading and trailing spaces" in {
      val actual = standardTextTransform("  ABC DEF  ")
      actual mustBe "ABC DEF"
    }
  }

}
