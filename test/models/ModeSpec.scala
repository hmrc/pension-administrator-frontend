/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class ModeSpec extends WordSpecLike with MustMatchers with OptionValues {

  "modePathBindable" must {
    val binder = Mode.modePathBindable

    "bind a valid CheckMode" in {
      binder.bind("mode", "change") mustBe Right(CheckMode)
    }

    "bind a valid mode" in {
      binder.bind("mode", "changing") mustBe Right(UpdateMode)
    }

    "bind a valid CheckUpdate" in {
      binder.bind("mode", "update") mustBe Right(CheckUpdateMode)
    }

    "fail to bind an unknown mode with negative value" in {
      binder.bind("mode", "invalid") mustBe Left("Mode binding failed")
    }

    "unbind a UpdateMode" in {
      binder.unbind("mode", UpdateMode) mustEqual "changing"
    }

    "unbind a CheckUpdateMode" in {
      binder.unbind("mode", CheckUpdateMode) mustEqual "update"
    }

    "unbind a CheckMode" in {
      binder.unbind("mode", CheckMode) mustEqual "change"
    }
  }
}
