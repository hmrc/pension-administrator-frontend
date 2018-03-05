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

package forms.register.company

import forms.mappings.Mappings
import javax.inject.Inject
import models.register.company.DirectorPreviousAddress
import play.api.data.Form
import play.api.data.Forms._

class DirectorPreviousAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[DirectorPreviousAddress] = Form(
     mapping(
      "field1" -> text("directorPreviousAddress.error.field1.required")
        .verifying(maxLength(35, "directorPreviousAddress.error.field1.length")),
      "field2" -> text("directorPreviousAddress.error.field2.required")
        .verifying(maxLength(35, "directorPreviousAddress.error.field2.length"))
    )(DirectorPreviousAddress.apply)(DirectorPreviousAddress.unapply)
   )
 }
