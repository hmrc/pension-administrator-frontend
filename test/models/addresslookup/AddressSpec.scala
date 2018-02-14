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

package models.addresslookup

import org.scalatest.{MustMatchers, WordSpec}

class AddressSpec extends WordSpec with MustMatchers {

  ".print" must {

    "print all of the fields of the address when they exist (minus country)" in {

      val model = Address("a", "b", Some("c"), Some("d"), Some("e"), "UK")

      model.print mustEqual "a, b, c, d, e"
    }

    "omit all the fields which are missing" in {

      val model = Address("a", "b", None, None, None, "UK")

      model.print mustEqual "a, b"
    }
  }
}
