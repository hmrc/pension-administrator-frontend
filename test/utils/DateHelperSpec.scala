/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate

class DateHelperSpec extends Matchers with AnyWordSpecLike {
  def buildDateHelper: DateHelper = new DateHelper {
    override private[utils] def currentDate: LocalDate = LocalDate.parse("2019-01-02")
  }
  val daysAhead = 28

  "dateAfterGivenDays" should {
    "return the date after the given days from todays date in the correct format" in {
      val result = buildDateHelper.dateAfterGivenDays(daysAhead).toString
      result shouldBe "30 January 2019"
    }
  }
}

