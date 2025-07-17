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

package models.register

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.{Enumerable, InputOption, WithName}

sealed trait NonUKBusinessType

object NonUKBusinessType extends Enumerable.Implicits {

  case object Company extends WithName("company") with NonUKBusinessType

  case object BusinessPartnership extends WithName("businessPartnership") with NonUKBusinessType

  val values: Seq[NonUKBusinessType] = Seq(
    Company,
    BusinessPartnership
  )

  def radioOptions(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        content = Text(messages(s"nonUKBusinessType.${value.toString}")),
        value=Some(value.toString))
  }

  def options: Seq[InputOption] =
    Seq(
      Company,
      BusinessPartnership
    ) map { value =>
      InputOption(value.toString, s"nonUKBusinessType.${value.toString}")
    }

  implicit val enumerable: Enumerable[NonUKBusinessType] =
    Enumerable(values.map(v => v.toString -> v)*)

}
