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

import forms.behaviours.UtrBehaviours
import play.api.data.Form

class UtrMappingSpec extends UtrBehaviours {

  "A form with a UTR" should {
    val mapping = utrMapping()

    val testForm: Form[String] = Form("utr" -> mapping)

    behave like formWithUtr(
      testForm,
      keyUtrRequired = "common.error.utr.required",
      keyUtrInvalid = "common.error.utr.invalid"
    )
  }

}
