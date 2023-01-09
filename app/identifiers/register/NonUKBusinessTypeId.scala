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

package identifiers.register

import identifiers._
import identifiers.register.company._
import identifiers.register.company.directors.DirectorId
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerId
import models.PersonName
import models.register.NonUKBusinessType
import models.register.NonUKBusinessType.{BusinessPartnership, Company}
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object NonUKBusinessTypeId extends TypedIdentifier[NonUKBusinessType] {
  override def toString: String = "businessType"

  override def cleanup(value: Option[NonUKBusinessType], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(Company) => removeAllPartnership(userAnswers)
      case Some(BusinessPartnership) => removeAllCompany(userAnswers)
      case _ => super.cleanup(value, userAnswers)
    }
  }

  private def removeDirectorsOrPartners(personNameSeq: Seq[PersonName],
                                        userAnswers: UserAnswers, id: TypedIdentifier[Nothing]): JsResult[UserAnswers] = {
    if (personNameSeq.nonEmpty) {
      userAnswers.remove(id)
    } else {
      JsSuccess(userAnswers)
    }
  }

  private def removeAllCompany(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessNameId, BusinessUTRId, CompanyAddressId, CompanySameContactAddressId,
      CompanyAddressListId, CompanyContactAddressId, CompanyContactAddressListId, CompanyAddressYearsId, CompanyPreviousAddressId,
      CompanyPreviousAddressPostCodeLookupId, CompanyEmailId, CompanyPhoneId, MoreThanTenDirectorsId))
      .flatMap(answers => removeDirectorsOrPartners(answers.allDirectors, answers, DirectorId))
  }

  private def removeAllPartnership(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessNameId, PartnershipRegisteredAddressId, PartnershipSameContactAddressId,
      PartnershipContactAddressPostCodeLookupId, PartnershipContactAddressListId, PartnershipContactAddressId,
      PartnershipAddressYearsId, PartnershipPreviousAddressId, PartnershipPreviousAddressPostCodeLookupId,
      PartnershipPreviousAddressListId, PartnershipEmailId, PartnershipPhoneId, MoreThanTenPartnersId))
      .flatMap(answers => removeDirectorsOrPartners(answers.allPartners, answers, PartnerId))
  }
}
