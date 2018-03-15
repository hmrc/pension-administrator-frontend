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

package identifiers.register.company.directors

import identifiers._
import models.AddressYears
import play.api.libs.json.JsPath
import utils.Cleanup

case class DirectorAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorAddressYearsId.toString
}

object DirectorAddressYearsId {
  override lazy val toString: String = "directorAddressYears"

  implicit lazy val addressYears: Cleanup[DirectorAddressYearsId] =
    Cleanup[AddressYears, DirectorAddressYearsId] {
      case (DirectorAddressYearsId(id), Some(AddressYears.OverAYear), answers) =>
        answers
          .remove(DirectorPreviousAddressPostCodeLookupId(id))
          .flatMap(_.remove(DirectorPreviousAddressId(id)))
    }

}
