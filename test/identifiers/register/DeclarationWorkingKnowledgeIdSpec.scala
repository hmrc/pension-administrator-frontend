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

package identifiers.register

import identifiers.register.adviser.{AdviserAddressId, AdviserAddressPostCodeLookupId, AdviserDetailsId}
import models.{Address, TolerantAddress}
import models.register.DeclarationWorkingKnowledge
import models.register.adviser.AdviserDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.{Enumerable, UserAnswers}
import play.api.libs.json.Json

class DeclarationWorkingKnowledgeIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {
 "Cleanup" when {

   val address=TolerantAddress(Some("test-address-line1"),
     Some("test-address-line2"),
     None,
     None,
     None,
     Some("test-country"))

   val answersWithAdviser = UserAnswers(Json.obj())
     .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
     .flatMap(_.set(AdviserDetailsId)(AdviserDetails("test name", "a@a", "01234567890")))
     .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq(address)))
     .flatMap(_.set(AdviserAddressId)(address.toAddress))
     .asOpt.value


   "where Declaration Working knowledge " must {
     val result: UserAnswers =
       answersWithAdviser.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
         .asOpt.value

     "remove the data for Postcode lookup" in {
       result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
     }
     "remove the data for address" in {
       result.get(AdviserAddressId) mustNot be(defined)
     }

     "remove date for adviser details" in {
       result.get(AdviserDetailsId) mustNot be(defined)
     }
   }

   "Declaration has an adviser" must {
     val result: UserAnswers = answersWithAdviser.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value

     "not remove the data for Postcode lookup" in {
       result.get(AdviserAddressPostCodeLookupId) mustBe defined
     }
     "not remove the data for address" in {
       result.get(AdviserAddressId) mustBe defined
     }

     "not remove date for adviser details" in {
       result.get(AdviserDetailsId) mustBe defined
     }
   }
 }
}
