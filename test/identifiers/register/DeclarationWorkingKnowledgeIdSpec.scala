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

package identifiers.register

import identifiers.register.adviser._
import models.TolerantAddress
import models.register.DeclarationWorkingKnowledge
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class DeclarationWorkingKnowledgeIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {
  "Cleanup" when {

    val address = TolerantAddress(Some("test-address-line1"),
      Some("test-address-line2"),
      None,
      None,
      None,
      Some("test-country"))

    val answersWithAdviser = UserAnswers(Json.obj())
      .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
      .flatMap(_.set(AdviserNameId)("test name"))
      .flatMap(_.set(AdviserEmailId)("a@a"))
      .flatMap(_.set(AdviserPhoneId)("01234567890"))
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq(address)))
      .flatMap(_.set(AdviserAddressId)(address.toAddress.get))
      .flatMap(_.set(AdviserAddressListId)(TolerantAddress(Some("100"),
        Some("SuttonStreet"),
        Some("Wokingham"),
        Some("Surrey"),
        Some("NE39 1HX"),
        Some("GB"))))
      .asOpt.value

    val answersWithAdviser2 = UserAnswers(Json.obj())
      .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser)
      .flatMap(_.set(AdviserNameId)("test name"))
      .flatMap(_.set(AdviserEmailId)("a@a"))
      .flatMap(_.set(AdviserPhoneId)("01234567890"))
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq(address)))
      .flatMap(_.set(AdviserAddressId)(address.toAddress.get))
      .flatMap(_.set(AdviserAddressListId)(TolerantAddress(Some("100"),
        Some("SuttonStreet"),
        Some("Wokingham"),
        Some("Surrey"),
        Some("NE39 1HX"),
        Some("GB"))))
      .asOpt.value


    "where Declaration Working knowledge " must {
      val result: UserAnswers =
        answersWithAdviser.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
          .asOpt.value

      "not remove the data for Postcode lookup (when nothing changed)" in {
        result.get(AdviserAddressPostCodeLookupId) must be(defined)
      }

      "not remove the data for address list (when nothing changed)" in {
        result.get(AdviserAddressListId) must be(defined)
      }

      "not remove the data for address (when nothing changed)" in {
        result.get(AdviserAddressId) must be(defined)
      }

      "not remove date for adviser email (when nothing changed)" in {
        result.get(AdviserEmailId) must be(defined)
      }

      "not remove date for adviser phone (when nothing changed)" in {
        result.get(AdviserPhoneId) must be(defined)
      }
    }

    "where Declaration have pension adviser" must {
      val result: UserAnswers =
        answersWithAdviser2.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
          .asOpt.value

      "remove the data for Postcode lookup" in {
        result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for address list" in {
        result.get(AdviserAddressListId) mustNot be(defined)
      }

      "remove the data for address" in {
        result.get(AdviserAddressId) mustNot be(defined)
      }

      "remove data for adviser email" in {
        result.get(AdviserEmailId) mustNot be(defined)
      }

      "remove data for adviser phone" in {
        result.get(AdviserPhoneId) mustNot be(defined)
      }
    }

    "Declaration has an adviser" must {
      val result: UserAnswers = answersWithAdviser.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value

      "not remove the data for Postcode lookup" in {
        result.get(AdviserAddressPostCodeLookupId) mustBe defined
      }

      "not remove the data for address list" in {
        result.get(AdviserAddressListId) mustBe defined
      }

      "not remove the data for address" in {
        result.get(AdviserAddressId) mustBe defined
      }

      "not remove data for adviser email" in {
        result.get(AdviserEmailId) mustBe defined
      }

      "not remove data for adviser phone" in {
        result.get(AdviserPhoneId) mustBe defined
      }
    }
  }
}
