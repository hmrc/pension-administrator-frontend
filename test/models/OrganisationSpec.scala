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

package models

import models.register.BusinessType.{BusinessPartnership, LimitedCompany, LimitedLiabilityPartnership, LimitedPartnership, OverseasCompany, UnlimitedCompany}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class OrganisationSpec extends AsyncFlatSpec with Matchers {

  private val organisationName = "test-org-name"

  "Organisation" should "match LimitedCompany to CorporateBody" in {
    Organisation(organisationName, LimitedCompany).organisationType shouldBe OrganisationType.CorporateBody
  }

  it should "match BusinessPartnership to Partnership" in {
    Organisation(organisationName, BusinessPartnership).organisationType shouldBe OrganisationType.Partnership
  }

  it should "match LimitedPartnership to Partnership" in {
    Organisation(organisationName, LimitedPartnership).organisationType shouldBe OrganisationType.Partnership
  }

  it should "match LimitedLiabilityPartnership to LLP" in {
    Organisation(organisationName, LimitedLiabilityPartnership).organisationType shouldBe OrganisationType.LLP
  }

  it should "match UnlimitedCompany to CorporateBody" in {
    Organisation(organisationName, UnlimitedCompany).organisationType shouldBe OrganisationType.CorporateBody
  }

  it should "not match OverseasCompany, throwing IllegalArgumentException" in {
    an[IllegalArgumentException] shouldBe thrownBy {
      Organisation(organisationName, OverseasCompany)
    }
  }

}
