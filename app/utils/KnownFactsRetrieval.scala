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

import identifiers.register.RegistrationInfoId
import identifiers.register.company.ConfirmCompanyAddressId
import models.RegistrationCustomerType.UK
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.register.{KnownFact, KnownFacts}
import models.requests.DataRequest
import play.api.mvc.AnyContent

class KnownFactsRetrieval {

  private val psaKey = "PSAID"
  private val ninoKey = "NINO"
  private val ctUtrKey = "CTUTR"
  private val saUtrKey = "SAUTR"
  private val postalKey = "NonUKPostalCode"
  private val countryKey = "CountryCode"

  def retrieve(psaId: String)(implicit request: DataRequest[AnyContent]): Option[KnownFacts] =
    request.userAnswers.get(RegistrationInfoId) flatMap { registrationInfo =>
      registrationInfo.legalStatus  match {
        case Individual =>
          Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(ninoKey, registrationInfo.idNumber))))
        case LimitedCompany if registrationInfo.customerType equals UK =>
          Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(ctUtrKey, registrationInfo.idNumber))))
        case Partnership if registrationInfo.customerType equals UK =>
          Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(saUtrKey, registrationInfo.idNumber))))
        case LimitedCompany | Partnership =>
          for {
            address <- request.userAnswers.get(ConfirmCompanyAddressId)
            country <- address.country
          } yield {
            val knownFacts = KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(countryKey, country)))
            address.postcode.fold(knownFacts) { postalCode =>
              KnownFacts(knownFacts.identifiers, knownFacts.verifiers + KnownFact(postalKey, transformNonUKPostalCode(postalCode)))
            }
          }
        case _ => None
      }

    }

  private val transformNonUKPostalCode: String => String = _.replaceAll(" ", "").toUpperCase

}
