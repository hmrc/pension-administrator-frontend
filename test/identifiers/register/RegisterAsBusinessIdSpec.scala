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

import java.time.LocalDate

import identifiers.register.NonUKBusinessTypeIdSpec.tolerantAddress
import identifiers.register.company._
import identifiers.register.company.directors.DirectorDetailsId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerDetailsId
import models._
import models.register.NonUKBusinessType
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class RegisterAsBusinessIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import RegisterAsBusinessIdSpec._

  "Cleanup for true if registering as Company" when {

    "register as company was true and business type was company and we change to false" must {
      val result: UserAnswers =
        answersCompany.set(RegisterAsBusinessId)(false)
          .asOpt.value

      "remove the data for non uk business type " in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
      }

      "remove the data for business details " in {
        result.get(BusinessDetailsId) mustNot be(defined)
      }

      "remove the data for company address " in {
        result.get(CompanyAddressId) mustNot be(defined)
        result.get(CompanyAddressListId) mustNot be(defined)
      }

      "remove the data for company contact address " in {
        result.get(CompanySameContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressListId) mustNot be(defined)
        result.get(CompanyContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for company previous address " in {
        result.get(CompanyAddressYearsId) mustNot be(defined)
        result.get(CompanyPreviousAddressId) mustNot be(defined)
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for contact details " in {
        result.get(ContactDetailsId) mustNot be(defined)
      }

      "remove the data for more than 10 directors " in {
        result.get(MoreThanTenDirectorsId) mustNot be(defined)
      }

      "remove the data for directors " in {
        result.get(DirectorDetailsId(0)) mustNot be(defined)
        result.get(DirectorDetailsId(1)) mustNot be(defined)
      }

      "not remove the data for individuals" in {
        result.get(IndividualDetailsId) must be(defined)
      }
    }

    "register as company was true and business type was partnership and we change to false" must {
      val result: UserAnswers =
        answersPartnership.set(RegisterAsBusinessId)(false)
          .asOpt.value

      "remove the data for non uk business type " in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
      }

      "remove the data for partnership details " in {
        result.get(PartnershipDetailsId) mustNot be(defined)
      }

      "remove the data for partnership address " in {
        result.get(PartnershipRegisteredAddressId) mustNot be(defined)
      }

      "remove the data for partnership contact address " in {
        result.get(PartnershipSameContactAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressListId) mustNot be(defined)
        result.get(PartnershipContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for partnership previous address " in {
        result.get(PartnershipAddressYearsId) mustNot be(defined)
        result.get(PartnershipPreviousAddressId) mustNot be(defined)
        result.get(PartnershipPreviousAddressListId) mustNot be(defined)
        result.get(PartnershipPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for more than 10 partners" in {
        result.get(MoreThanTenPartnersId) mustNot be(defined)
      }

      "remove the data for contact details " in {
        result.get(PartnershipContactDetailsId) mustNot be(defined)
      }

      "remove the data for directors " in {
        result.get(PartnerDetailsId(0)) mustNot be(defined)
        result.get(PartnerDetailsId(1)) mustNot be(defined)
      }
    }
  }

  "Cleanup for false if registering as Individual" when {

    "register as company was false and we change to true" must {
      val result: UserAnswers =
        answersIndividual.set(RegisterAsBusinessId)(true)
          .asOpt.value

      "remove the data for individual details " in {
        result.get(IndividualDetailsId) mustNot be(defined)
      }

      "remove the data for dob " in {
        result.get(IndividualDateOfBirthId) mustNot be(defined)
      }

      "remove the data for individual address " in {
        result.get(IndividualAddressId) mustNot be(defined)
      }

      "remove the data for individual contact address " in {
        result.get(IndividualSameContactAddressId) mustNot be(defined)
        result.get(IndividualContactAddressListId) mustNot be(defined)
        result.get(IndividualContactAddressId) mustNot be(defined)
      }

      "remove the data for individual previous address " in {
        result.get(IndividualAddressYearsId) mustNot be(defined)
        result.get(IndividualPreviousAddressId) mustNot be(defined)
        result.get(IndividualPreviousAddressListId) mustNot be(defined)
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for contact details " in {
        result.get(IndividualContactDetailsId) mustNot be(defined)
      }

      "not remove the data for non uk business type " in {
        result.get(NonUKBusinessTypeId) must be(defined)
      }
    }
  }
}

object RegisterAsBusinessIdSpec extends OptionValues {

  val tolerantAddress = TolerantAddress(Some("line 1"), Some("line 2"), Some("line 3"), Some("line 4"), None, Some("DE"))
  val tolerantIndividual = TolerantIndividual(Some("firstName"), Some("middleName"), Some("lastName"))
  val address = Address("line 1", "line 2", None, None, None, "GB")
  val contactDetails = ContactDetails("s@s.com", "999")
  val personDetails = PersonDetails("test first", None, "test last", LocalDate.now())

  val answersCompany: UserAnswers = UserAnswers(Json.obj())
    .set(RegisterAsBusinessId)(true)
    .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.Company)
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("company name", None)))
      .flatMap(_.set(CompanyAddressId)(tolerantAddress))
      .flatMap(_.set(CompanySameContactAddressId)(false))
      .flatMap(_.set(CompanyContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(CompanyAddressListId)(tolerantAddress))
      .flatMap(_.set(CompanyContactAddressId)(address))
      .flatMap(_.set(CompanyContactAddressListId)(tolerantAddress))
      .flatMap(_.set(CompanyAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(CompanyPreviousAddressId)(address))
      .flatMap(_.set(CompanyPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(ContactDetailsId)(contactDetails))
      .flatMap(_.set(DirectorDetailsId(0))(personDetails))
      .flatMap(_.set(DirectorDetailsId(1))(personDetails))
      .flatMap(_.set(MoreThanTenDirectorsId)(true))
      .flatMap(_.set(IndividualDetailsId)(tolerantIndividual))
    )
    .asOpt.value

  val answersPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(RegisterAsBusinessId)(true)
    .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership)
      .flatMap(_.set(PartnershipDetailsId)(BusinessDetails("company name", None)))
      .flatMap(_.set(PartnershipRegisteredAddressId)(tolerantAddress))
      .flatMap(_.set(PartnershipSameContactAddressId)(false))
      .flatMap(_.set(PartnershipContactAddressListId)(tolerantAddress))
      .flatMap(_.set(PartnershipContactAddressId)(address))
      .flatMap(_.set(PartnershipAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(PartnershipPreviousAddressId)(address))
      .flatMap(_.set(PartnershipPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(PartnershipContactDetailsId)(contactDetails))
      .flatMap(_.set(PartnerDetailsId(0))(personDetails))
      .flatMap(_.set(PartnerDetailsId(1))(personDetails))
      .flatMap(_.set(MoreThanTenPartnersId)(true))
    )
    .asOpt.value

  val answersIndividual: UserAnswers = UserAnswers(Json.obj())
    .set(RegisterAsBusinessId)(false)
    .flatMap(_.set(IndividualDetailsId)(tolerantIndividual)
      .flatMap(_.set(IndividualAddressId)(tolerantAddress))
      .flatMap(_.set(IndividualDateOfBirthId)(LocalDate.now()))
      .flatMap(_.set(IndividualSameContactAddressId)(false))
      .flatMap(_.set(IndividualContactAddressListId)(tolerantAddress))
      .flatMap(_.set(PartnershipContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(IndividualContactAddressId)(address))
      .flatMap(_.set(IndividualAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(IndividualPreviousAddressId)(address))
      .flatMap(_.set(IndividualPreviousAddressListId)(tolerantAddress))
      .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(IndividualContactDetailsId)(contactDetails))
      .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.Company))
    )
    .asOpt.value

}

