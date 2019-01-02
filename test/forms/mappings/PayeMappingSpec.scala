/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.behaviours.{PayeBehaviours, StringFieldBehaviours}
import models.Paye
import play.api.data.Form

class PayeMappingSpec extends PayeBehaviours with StringFieldBehaviours {

  "A form with Paye Number" should {
    val mapping = payeMapping()

    val testForm: Form[Paye] = Form("paye" -> mapping)
    val rawData = Map("paye.hasPaye" -> "true", "paye.paye" -> " 123\\/4567898765 ")
    val expectedData = Paye.Yes("1234567898765")

    behave like formWithPaye(
      testForm,
      requiredKey = "common.radio.error.required",
      keyPayeRequired = "common.error.paye.required",
      keyPayeLength = "common.error.paye.length"
    )

    behave like formWithTransform[Paye](
      testForm,
      rawData,
      expectedData
    )
  }
}
