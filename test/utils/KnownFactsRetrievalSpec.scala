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

import base.SpecBase
import identifiers.register.RegistrationInfoId
import identifiers.register.company.{CompanyAddressId, ConfirmCompanyAddressId}
import identifiers.register.partnership.PartnershipRegisteredAddressId
import models._
import models.register.{KnownFact, KnownFacts}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino

class KnownFactsRetrievalSpec extends SpecBase {

  private val psa = "psa-id"
  private val utr = "test-utr"
  private val nino = Nino("AB123456C")
  private val sapNumber = "test-sap-number"
  private val externalId = "test-externalId"
  private val nonUk = "test-non-uk"

  private lazy val generator = app.injector.instanceOf[KnownFactsRetrieval]

  "retrieve" must {

    "return set of known facts" when {

      "user is individual" which {

        "comprise of NINO" in {

          val registration = RegistrationInfo(
            RegistrationLegalStatus.Individual,
            sapNumber,
            noIdentifier = false,
            RegistrationCustomerType.UK,
            Some(RegistrationIdType.Nino),
            Some(nino.nino)
          )

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            externalId,
            PSAUser(UserType.Individual, Some(nino), isExistingPSA = false, None, None, ""),
            UserAnswers(Json.obj(
              ConfirmCompanyAddressId.toString -> TolerantAddress(
                Some("1 Street"),
                Some("Somewhere"),
                None, None,
                Some("ZZ1 1ZZ"),
                Some("GB")
              ),
              RegistrationInfoId.toString -> registration
            ))
          )

          generator.retrieve(psa) mustEqual Some(KnownFacts(
            Set(KnownFact("PSAID", psa)),
            Set(KnownFact("NINO", nino.nino)
            )))

        }

      }

      "user is partnership" which {

        "comprise of SA UTR" when {

          "company is UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.Partnership,
              sapNumber,
              noIdentifier = false,
              RegistrationCustomerType.UK,
              Some(RegistrationIdType.UTR),
              Some(utr)
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
              UserAnswers(Json.obj(
                ConfirmCompanyAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None,
                  Some("ZZ1 1ZZ"),
                  Some("GB")
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve(psa) mustEqual Some(KnownFacts(
              Set(KnownFact("PSAID", psa)),
              Set(KnownFact("SAUTR", utr)
              )))
          }

        }

        "comprise of PSA ID and Country Code" when {

          "company is Non-UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.Partnership,
              sapNumber,
              noIdentifier = false,
              RegistrationCustomerType.NonUK,
              None,
              None
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
              UserAnswers(Json.obj(
                PartnershipRegisteredAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None, None,
                  Some(nonUk)
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve(psa) mustEqual Some(KnownFacts(
              Set(KnownFact("PSAID", psa)),
              Set(KnownFact("CountryCode", nonUk)
              )))
          }
        }
      }

      "user is company" which {

        "comprise of CTR UTR" when {

          "company is UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.LimitedCompany,
              sapNumber,
              noIdentifier = false,
              RegistrationCustomerType.UK,
              Some(RegistrationIdType.UTR),
              Some(utr)
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
              UserAnswers(Json.obj(
                ConfirmCompanyAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None,
                  Some("ZZ1 1ZZ"),
                  Some("GB")
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve(psa) mustEqual Some(KnownFacts(
              Set(KnownFact("PSAID", psa)),
              Set(KnownFact("CTUTR", utr)
              )))
          }

        }

        "comprise of PSA ID and Country Code" when {

          "company is Non-UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.LimitedCompany,
              sapNumber,
              noIdentifier = false,
              RegistrationCustomerType.NonUK,
              None,
              None
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
              UserAnswers(Json.obj(
                CompanyAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None, None,
                  Some(nonUk)
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve(psa) mustEqual Some(KnownFacts(
              Set(KnownFact("PSAID", psa)),
              Set(KnownFact("CountryCode", nonUk)
              )))
          }

        }

      }

    }

  }

}
