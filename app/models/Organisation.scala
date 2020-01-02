/*
 * Copyright 2020 HM Revenue & Customs
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

import models.register.BusinessType
import models.register.BusinessType.{BusinessPartnership, LimitedCompany, LimitedLiabilityPartnership, LimitedPartnership, UnlimitedCompany}
import play.api.libs.json.{Json, _}
import utils.EnumUtils

object OrganisationTypeEnum extends Enumeration {
  type OrganisationType = Value
  val CorporateBody: OrganisationTypeEnum.Value = Value("Corporate Body")
  val NotSpecified: OrganisationTypeEnum.Value = Value("Not Specified")
  val LLP: OrganisationTypeEnum.Value = Value("LLP")
  val Partnership: OrganisationTypeEnum.Value = Value("Partnership")
  val UnincorporatedBody: OrganisationTypeEnum.Value = Value("Unincorporated Body")

  implicit def enumFormats: Format[OrganisationType] = EnumUtils.enumFormat(OrganisationTypeEnum)
}

case class Organisation(organisationName: String, organisationType: OrganisationTypeEnum.OrganisationType)

object Organisation {

  def apply(organisationName: String, businessType: BusinessType): Organisation = {
    val organisationType = businessType match {
      case LimitedCompany => OrganisationTypeEnum.CorporateBody
      case BusinessPartnership => OrganisationTypeEnum.Partnership
      case LimitedPartnership => OrganisationTypeEnum.Partnership
      case LimitedLiabilityPartnership => OrganisationTypeEnum.LLP
      case UnlimitedCompany => OrganisationTypeEnum.CorporateBody
      case _ => throw new IllegalArgumentException(s"Business type ${businessType.toString} cannot be mapped to OrganisationTypeEnum")
    }

    Organisation(organisationName, organisationType)
  }

  implicit val formats: OFormat[Organisation] = Json.format[Organisation]

}
