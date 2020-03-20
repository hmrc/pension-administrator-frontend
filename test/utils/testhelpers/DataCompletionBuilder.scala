/*
 * Copyright 2020 HM Revenue & Customs
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

package utils.testhelpers

import java.time.LocalDate

import models.{Address, AddressYears, PersonName}
import org.scalatest.OptionValues
import utils.UserAnswers

object DataCompletionBuilder {

  implicit class DataCompletionUserAnswerOps(answers: UserAnswers) extends OptionValues {
    val address = Address("Telford1", "Telford2", Some("Telford3"), Some("Telford4"), Some("TF3 4ER"), "GB")

    def completeDirector(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.directorName(index, PersonName(s"first$index", s"last$index", isDeleted)).directorDob(index, LocalDate.now().minusYears(20)).
        directorHasNINO(index, flag = false).directorNoNINOReason(index, reason = "no nino").directorHasUTR(index, flag = false).
        directorNoUTRReason(index, reason = "no utr").directorAddress(index, address).directorAddressYears(index, AddressYears.OverAYear).
        directorEmail(index, email = "s@s.com").directorPhone(index, phone = "123")
    }

    def completePartner(index: Int): UserAnswers = {
      answers.partnerName(index, PersonName(s"first$index", s"last$index")).partnerDob(index, LocalDate.now().minusYears(20)).
        partnerHasNINO(index, flag = false).partnerNoNINOReason(index, reason = "no nino").partnerHasUTR(index, flag = false).
        partnerNoUTRReason(index, reason = "no utr").partnerAddress(index, address).partnerAddressYears(index, AddressYears.OverAYear).
        partnerEmail(index, email = "s@s.com").partnerPhone(index, phone = "123")
    }
  }
}
