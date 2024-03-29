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


import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class DateHelper {
  private[utils] def currentDate: LocalDate = LocalDate.now()

  private val simpleFormatter = new SimpleDateFormat("dd MMMM YYYY")
  private val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val formatterWithSlash =DateTimeFormatter.ofPattern("d/MM/uuuu")

  def formatDate(date: LocalDate): String = date.format(formatter)

  def formatDateWithSlash(date: LocalDate): String = date.format(formatterWithSlash)

  def dateAfterGivenDays(daysAhead: Int): String = formatDate(currentDate.plusDays(daysAhead))

  def fromMillis(millis: Long): String = simpleFormatter.format(new Date(millis))
}

object DateHelper extends DateHelper
