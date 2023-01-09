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

package viewmodels

import base.SpecBase
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers

class MessageSpec extends SpecBase with Matchers with OptionValues {

  "resolve" must {

    "explicitly resolve a literal string to itself" in {
      val message: Message = "common.firstname"
      message.resolve mustEqual "common.firstname"
    }

    "implicitly resolve a literal string to itself" in {
      val message: Message = "common.firstname"
      (message: String) mustEqual "common.firstname"
    }

    "explicitly resolve a message key to its value" in {
      val message: Message = Message("common.firstname")
      message.resolve(messages) mustEqual messages("common.firstname")
    }

    "implicitly resolve a message key to its value" in {
      val message: String = Message("common.firstname")
      message mustEqual messages("common.firstname")
    }

    "implicitly resolve an optional message" in {
      val message: Option[String] = Some(Message("common.firstname"))
      message.value mustEqual messages("common.firstname")
    }

    "resolve a message with args" in {
      val message: Message = Message("common.postcodeLookup.enterPostcode", "foo", "bar")
      message.resolve mustEqual messages("common.postcodeLookup.enterPostcode", "foo", "bar")
    }
  }

  "withArgs" must {

    "return a copy of a Message with a new args list" in {
      val message = Message("foo", "bar")
      val newMessage = message.withArgs("baz")
      newMessage mustEqual Message("foo", "baz")
    }
  }
}
