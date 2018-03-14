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

package forms.register.company.directors

import forms.behaviours.OptionFieldBehaviours
import models.register.company.directors.DirectorUniqueTaxReference

class DirectorUniqueTaxReferenceFormProviderSpec extends OptionFieldBehaviours {

  val requiredKey = "directorUniqueTaxReference.error.required"
  val formProvider = new DirectorUniqueTaxReferenceFormProvider()()

  "DirectorUniqueTaxReference form provider" must {

    "successfully bind when yes is selected and valid utr is provided" in {
      val form = formProvider.bind(Map("directorUtr.hasUtr" -> "true", "directorUtr.utr" -> "1234567890"))
      form.get shouldBe DirectorUniqueTaxReference.Yes("1234567890")
    }

    "successfully bind when no is selected and reason is provided" in {
      val form = formProvider.bind(Map("directorUtr.hasUtr" -> "false", "directorUtr.reason" -> "haven't got utr"))
      form.get shouldBe DirectorUniqueTaxReference.No("haven't got utr")
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("directorUtr.hasUtr", requiredKey)
      checkForError(formProvider, emptyForm, expectedError)
    }
  }
}
