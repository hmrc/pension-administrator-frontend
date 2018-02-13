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

package models.register.company

import utils.{Enumerable, InputOption, WithName}

sealed trait CompanyAddressYears

object CompanyAddressYears extends Enumerable.Implicits {

  case object UnderAYear extends WithName("underAYear") with CompanyAddressYears
  case object OverAYear extends WithName("overAYear") with CompanyAddressYears

  val values: Seq[CompanyAddressYears] = Seq(
    UnderAYear, OverAYear
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"companyAddressYears.${value.toString}")
  }

  implicit val enumerable: Enumerable[CompanyAddressYears] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
