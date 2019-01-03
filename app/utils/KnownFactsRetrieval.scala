/*
 * Copyright 2019 HM Revenue & Customs
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
import identifiers.register.company.CompanyAddressId
import identifiers.register.individual.IndividualAddressId
import identifiers.register.partnership.PartnershipRegisteredAddressId
import models.RegistrationCustomerType.{NonUK, UK}
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

    (registrationInfo.legalStatus, registrationInfo.idNumber, registrationInfo.customerType) match {
      case (Individual, Some(idNumber), UK) =>
        Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(ninoKey, idNumber))))
      case (LimitedCompany, Some(idNumber), UK) =>
        Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(ctUtrKey, idNumber))))
      case (Partnership, Some(idNumber), UK) =>
        Some(KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(saUtrKey, idNumber))))
      case (legalStatus, _, NonUK) =>
        for {
          address <- legalStatus match {
            case LimitedCompany => request.userAnswers.get(CompanyAddressId)
            case Partnership => request.userAnswers.get(PartnershipRegisteredAddressId)
            case Individual => request.userAnswers.get(IndividualAddressId)
          }
          country <- address.country
        } yield {
          KnownFacts(Set(KnownFact(psaKey, psaId)), Set(KnownFact(countryKey, country)))
        }
      case _ => None
    }

  }

}
