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

package models.register

import utils.{Enumerable, InputOption, WithName}

sealed trait BusinessType

object BusinessType extends Enumerable.Implicits {

  case object LimitedCompany extends WithName("limitedCompany") with BusinessType
  case object BusinessPartnership extends WithName("businessPartnership") with BusinessType
  case object LimitedPartnership extends WithName("limitedPartnership") with BusinessType
  case object LimitedLiabilityPartnership extends WithName("limitedLiabilityPartnership") with BusinessType
  case object UnlimitedCompany extends WithName("unlimitedCompany") with BusinessType
  case object OverseasCompany extends WithName("overseasCompany") with BusinessType

  val values: Seq[BusinessType] = Seq(
    LimitedCompany,
    BusinessPartnership,
    LimitedPartnership,
    LimitedLiabilityPartnership,
    UnlimitedCompany,
    OverseasCompany
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"businessType.${value.toString}")
  }

  implicit val enumerable: Enumerable[BusinessType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
