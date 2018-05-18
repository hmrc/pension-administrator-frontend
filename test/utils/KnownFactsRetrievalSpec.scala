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

import base.SpecBase
import identifiers.register.company.ConfirmCompanyAddressId
import identifiers.register.{PsaSubscriptionResponseId, RegistrationInfoId}
import models._
import models.register.{KnownFact, KnownFacts, PsaSubscriptionResponse}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

class KnownFactsRetrievalSpec extends SpecBase {

  private val utr = "test-utr"
  private val nino = "test-nino"
  private val sapNumber = "test-sap-number"
  private val externalId = "test-externalId"
  private val nonUk = "test-non-uk"
  private val postalCode = "test pcode"

  lazy val generator = app.injector.instanceOf[KnownFactsRetrieval]

  "retrieve" must {

    "return set of known facts" when {

      "user is individual" which {

        "comprise of NINO" in {

          val registration = RegistrationInfo(
            RegistrationLegalStatus.Individual,
            sapNumber,
            false,
            RegistrationCustomerType.UK,
            RegistrationIdType.Nino,
            nino
          )

          implicit val request: DataRequest[AnyContent] = DataRequest(
            FakeRequest(),
            externalId,
            PSAUser(UserType.Individual, Some(nino), false, None),
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

          generator.retrieve mustEqual Some(KnownFacts(Set(
            KnownFact("NINO", nino)
          )))

        }

      }

      "user is company" which {

        "comprise of CTR UTR" when {

          "company is UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.LimitedCompany,
              sapNumber,
              false,
              RegistrationCustomerType.UK,
              RegistrationIdType.UTR,
              utr
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, false, None),
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

            generator.retrieve mustEqual Some(KnownFacts(Set(
              KnownFact("CTUTR", utr)
            )))
          }

        }

        "comprise of Postal ID and Country Code" when {

          "company is Non-UK" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.LimitedCompany,
              sapNumber,
              false,
              RegistrationCustomerType.NonUK,
              RegistrationIdType.UTR,
              utr
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, false, None),
              UserAnswers(Json.obj(
                ConfirmCompanyAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None,
                  Some(postalCode),
                  Some(nonUk)
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve mustEqual Some(KnownFacts(Set(
              KnownFact("NonUKPostalCode", "TESTPCODE"),
              KnownFact("CountryCode", nonUk)
            )))
          }

        }

        "comprise of PSA ID and Country Code" when {

          "company is Non-UK and Postal ID does not exist" in {

            val registration = RegistrationInfo(
              RegistrationLegalStatus.LimitedCompany,
              sapNumber,
              false,
              RegistrationCustomerType.NonUK,
              RegistrationIdType.UTR,
              utr
            )

            implicit val request: DataRequest[AnyContent] = DataRequest(
              FakeRequest(),
              externalId,
              PSAUser(UserType.Organisation, None, false, None),
              UserAnswers(Json.obj(
                ConfirmCompanyAddressId.toString -> TolerantAddress(
                  Some("1 Street"),
                  Some("Somewhere"),
                  None, None, None,
                  Some(nonUk)
                ),
                RegistrationInfoId.toString -> registration
              ))
            )

            generator.retrieve mustEqual Some(KnownFacts(Set(
              KnownFact("CountryCode", nonUk)
            )))
          }

        }

      }

    }

  }

}
