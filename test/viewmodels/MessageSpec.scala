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

package viewmodels

import base.SpecBase
import org.scalatest.{MustMatchers, OptionValues}

class MessageSpec extends SpecBase with MustMatchers with OptionValues {

  "resolve" must {

    "explicitly resolve a literal string to itself" in {
      val message: Message = "messages__common__first_name"
      message.resolve mustEqual "messages__common__first_name"
    }

    "implicitly resolve a literal string to itself" in {
      val message: Message = "messages__common__first_name"
      (message: String) mustEqual "messages__common__first_name"
    }

    "explicitly resolve a message key to its value" in {
      val message: Message = Message("messages__common__first_name")
      message.resolve(messages) mustEqual messages("messages__common__first_name")
    }

    "implicitly resolve a message key to its value" in {
      val message: String = Message("messages__common__first_name")
      message mustEqual messages("messages__common__first_name")
    }

    "implicitly resolve an optional message" in {
      val message: Option[String] = Some(Message("messages__common__first_name"))
      message.value mustEqual messages("messages__common__first_name")
    }

    "resolve a message with args" in {
      val message: Message = Message("messages__common__postcode_lookup__enter_postcode", "foo", "bar")
      message.resolve mustEqual messages("messages__common__postcode_lookup__enter_postcode", "foo", "bar")
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
