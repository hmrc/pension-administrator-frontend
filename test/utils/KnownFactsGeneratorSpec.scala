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

class KnownFactsGeneratorSpec extends SpecBase {

  "constructKnownFacts" must {

    "return set of known facts" when {

      "user is individual" which {

        "comprise of PSA ID and NINO" in {

        }

      }

      "user is company" which {

        "comprise of PSA ID and CTR UTR" when {

          "company is UK" in {

          }

        }

        "comprise of PSA ID, Postal ID and Country Code" when {

          "company is Non-UK" in {

          }

        }

      }

    }

  }

}
