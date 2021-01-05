/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.Mapping

trait BusinessNameMapping extends Mappings with Transforms {

  def nameMapping(keyNameRequired: String, keyNameInvalid: String, keyNameMaxLength: String): Mapping[String] = {
    text(keyNameRequired)
      .transform(standardTextTransform, noTransform)
      .verifying(
        firstError(
          maxLength(
            BusinessNameMapping.maxLength,
            keyNameMaxLength
          ),
          businessName(keyNameInvalid)
        )
      )
  }

}

object BusinessNameMapping {
  val maxLength = 105
}
