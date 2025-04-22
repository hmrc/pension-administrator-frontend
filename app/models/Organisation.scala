/*
 * Copyright 2024 HM Revenue & Customs
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

//import models.OrganisationTypeEnum.OrganisationType
import models.register.BusinessType
import models.register.BusinessType.{BusinessPartnership, LimitedCompany, LimitedLiabilityPartnership, LimitedPartnership, UnlimitedCompany}
import play.api.libs.json.{Json, *}
//import utils.EnumUtils
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

//object OrganisationTypeEnum extends Enumeration {
//  type OrganisationType = Value
//
//  val CorporateBody: OrganisationTypeEnum.Value = Value("Corporate Body")
//  val NotSpecified: OrganisationTypeEnum.Value = Value("Not Specified")
//  val LLP: OrganisationTypeEnum.Value = Value("LLP")
//  val Partnership: OrganisationTypeEnum.Value = Value("Partnership")
//  val UnincorporatedBody: OrganisationTypeEnum.Value = Value("Unincorporated Body")
//
//  val en:OrganisationType = Value //.asInstanceOf[OrganisationType]
//  val eu: EnumUtils[OrganisationType] = new EnumUtils[OrganisationType](using en) {}
//  implicit def enumFormats: Format[OrganisationType] = eu.enumFormat(en.nn)
//}

enum OrganisationType extends Enum[OrganisationType] {
  case CorporateBody
  case NotSpecified
  case LLP
  case Partnership
  case UnincorporatedBody

  override def toString: String = this.name match {
    case "CorporateBody" => "Corporate Body"
    case "NotSpecified" => "Not Specified"
    case "LLP" => "LLP"
    case "Partnership" => "Partnership"
    case "UnincorporatedBody" => "Unincorporated Body"
  }
}

object OrganisationType {
  private def fromString(value: String): Option[OrganisationType] =
    OrganisationType.values.toSeq.find(_.name() == value)

  implicit def reads: Reads[OrganisationType] = {
    case JsString(s) =>
      Try[OrganisationType](OrganisationType.fromString(s).get) match {
        case Failure(orgType) => JsError("JourneyType value expected")
        case Success(orgType) => JsSuccess(orgType)
      }
    case _ => JsError("String value expected")
  }

  implicit def writes: Writes[OrganisationType] = (v: OrganisationType) => JsString(v.name())
}

case class Organisation(organisationName: String, organisationType: OrganisationType)

object Organisation {

  def apply(organisationName: String, businessType: BusinessType): Organisation = {
    val organisationType = businessType match {
      case LimitedCompany => OrganisationType.CorporateBody
      case BusinessPartnership => OrganisationType.Partnership
      case LimitedPartnership => OrganisationType.Partnership
      case LimitedLiabilityPartnership => OrganisationType.LLP
      case UnlimitedCompany => OrganisationType.CorporateBody
      case _ => throw new IllegalArgumentException(s"Business type ${businessType.toString} cannot be mapped to OrganisationTypeEnum")
    }

    Organisation(organisationName, organisationType)
  }

  implicit val formats: Format[Organisation] = Json.format[Organisation]

}
