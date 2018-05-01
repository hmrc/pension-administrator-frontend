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

import identifiers.register.advisor.{AdvisorAddressId, AdvisorAddressPostCodeLookupId, AdvisorDetailsId}
import models.Address
import models.register.DeclarationWorkingKnowledge
import models.register.advisor.AdvisorDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.{Enumerable, UserAnswers}
import play.api.libs.json.Json

class DeclarationWorkingKnowledgeIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {
 "Cleanup" when {

   val address=Address("test-address-line1","test-address-line2", None,None, None,"test-country")

   val answersWithAdvisor = UserAnswers(Json.obj())
     .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
     .flatMap(_.set(AdvisorDetailsId)(AdvisorDetails("test name", "a@a", "01234567890")))
     .flatMap(_.set(AdvisorAddressPostCodeLookupId)(Seq(address)))
     .flatMap(_.set(AdvisorAddressId)(address))
     .asOpt.value


   "where Declaration Working knowledge " must {
     val result: UserAnswers =
       answersWithAdvisor.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge)
         .asOpt.value

     "remove the data for Postcode lookup" in {
       result.get(AdvisorAddressPostCodeLookupId) mustNot be(defined)
     }
     "remove the data for address" in {
       result.get(AdvisorAddressId) mustNot be(defined)
     }

     "remove date for advisor details" in {
       result.get(AdvisorDetailsId) mustNot be(defined)
     }
   }

   "Declaration has an advisor" must {
     val result: UserAnswers = answersWithAdvisor.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value

     "not remove the data for Postcode lookup" in {
       result.get(AdvisorAddressPostCodeLookupId) mustBe defined
     }
     "not remove the data for address" in {
       result.get(AdvisorAddressId) mustBe defined
     }

     "not remove date for advisor details" in {
       result.get(AdvisorDetailsId) mustBe defined
     }
   }
 }
}
