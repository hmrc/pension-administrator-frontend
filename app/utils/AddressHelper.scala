/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.TolerantAddress

class AddressHelper {
  def mapAddressFields(address: TolerantAddress): Map[String, String] = {
    Map(
      "addressLine1" -> address.addressLine1.getOrElse(""),
      "addressLine2" -> address.addressLine2.getOrElse(""),
      "addressLine3" -> address.addressLine3.getOrElse(""),
      "addressLine4" -> address.addressLine4.getOrElse(""),
      "postCode" -> address.postcode.getOrElse(""),
      "country" -> address.countryOpt.getOrElse("")
    )
  }
}
