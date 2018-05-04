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

package utils

import identifiers.register.company.ConfirmCompanyAddressId
import identifiers.register.{PsaSubscriptionResponseId, RegistrationInfoId}
import models.RegistrationCustomerType.UK
import models.RegistrationLegalStatus.{Individual, LimitedCompany}
import models.register.{KnownFact, KnownFacts}
import models.requests.DataRequest
import play.api.mvc.AnyContent

class KnownFactsGenerator {

  private val psaIdKey = "PSAID"
  private val ninoKey = "NINO"
  private val ctutrKey = "CTUTR"
  private val postalKey = "NonUKPostalCode"
  private val countryKey = "CountryCode"

  def constructKnownFacts(implicit request: DataRequest[AnyContent]): Option[KnownFacts] = {

    val knownFacts: Option[Set[KnownFact]] = for {
      psaSubscriptionResponse <- request.userAnswers.get(PsaSubscriptionResponseId)
      confirmCompanyAddress  <- request.userAnswers.get(ConfirmCompanyAddressId)
      registrationInfo <- request.userAnswers.get(RegistrationInfoId)
    } yield {

      registrationInfo.legalStatus match {
        case Individual =>
          Set(KnownFact(psaIdKey, psaSubscriptionResponse.psaId), KnownFact(ninoKey, registrationInfo.idNumber))
        case LimitedCompany if registrationInfo.customerType equals UK =>
          Set(KnownFact(psaIdKey, psaSubscriptionResponse.psaId), KnownFact(ctutrKey, registrationInfo.idNumber))
        case LimitedCompany => {
          val knownFacts = Set(KnownFact(psaIdKey, psaSubscriptionResponse.psaId), KnownFact(countryKey, confirmCompanyAddress.toAddress.country))
          knownFacts
        }
        case _ =>
          Set.empty[KnownFact]
      }

    }

    knownFacts match {
      case Some(kf) if kf.nonEmpty => Some(KnownFacts(kf))
      case _ => None
    }

  }

}