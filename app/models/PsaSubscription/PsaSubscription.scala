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

package models.PsaSubscription

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}

case class CustomerIdentification(legalStatus: String, typeOfId: Option[String], number: Option[String], isOverseasCustomer: Boolean)

object CustomerIdentification {
  implicit val formatsAddress: Format[CustomerIdentification] = Json.format[CustomerIdentification]
}

case class CorrespondenceDetails(address: CorrespondenceAddress, contactDetails: Option[PsaContactDetails])

object CorrespondenceDetails {
  implicit val formatsAddress: Format[CorrespondenceDetails] = Json.format[CorrespondenceDetails]
}

case class OrganisationOrPartner(name: String, crn: Option[String], vatRegistration: Option[String], paye: Option[String])

object OrganisationOrPartner {
  implicit val formatsAddress: Format[OrganisationOrPartner] = Json.format[OrganisationOrPartner]
}

case class PensionAdvisor(name: String, address: CorrespondenceAddress, contactDetails: Option[PsaContactDetails])

object PensionAdvisor {
  implicit val formatsAddress: Format[PensionAdvisor] = Json.format[PensionAdvisor]
}

case class PsaContactDetails(telephone: String, email: Option[String])

object PsaContactDetails {
  implicit val formatsAddress: Format[PsaContactDetails] = Json.format[PsaContactDetails]
}

case class CorrespondenceAddress(addressLine1: String, addressLine2: String, addressLine3: Option[String],
                                 addressLine4: Option[String], countryCode: String, postalCode: Option[String])

object CorrespondenceAddress {
  implicit val formatsAddress: Format[CorrespondenceAddress] = Json.format[CorrespondenceAddress]
}

case class IndividualDetailType(title: Option[String] = None, firstName: String, middleName: Option[String] = None,
                                lastName: String, dateOfBirth: java.time.LocalDate){
  def fullName: String = middleName match {
    case Some(middle) => s"$firstName $middle $lastName"
    case None => s"$firstName $lastName"
  }
}

object IndividualDetailType {
  implicit val formatsAddress: Format[IndividualDetailType] = Json.format[IndividualDetailType]
}

case class DirectorOrPartner(isDirectorOrPartner: String,
                             title: Option[String],
                             firstName: String,
                             middleName: Option[String],
                             lastName: String,
                             dateOfBirth: LocalDate,
                             nino: Option[String],
                             utr: Option[String],
                             isSameAddressForLast12Months: Boolean,
                             previousAddress: Option[CorrespondenceAddress],
                             correspondenceDetails: Option[CorrespondenceDetails]){
  def fullName: String = middleName match {
    case Some(middle) => s"$firstName $middle $lastName"
    case None => s"$firstName $lastName"
  }
}

object DirectorOrPartner {
  implicit val formatsAddress: Format[DirectorOrPartner] = Json.format[DirectorOrPartner]
}

case class PsaSubscription(isSuspended: Boolean, customerIdentification: CustomerIdentification,
                           organisationOrPartner: Option[OrganisationOrPartner], individual: Option[IndividualDetailType], address: CorrespondenceAddress,
                           contact: PsaContactDetails, isSameAddressForLast12Months: Boolean, previousAddress: Option[CorrespondenceAddress],
                           directorsOrPartners: Option[Seq[DirectorOrPartner]], pensionAdvisor: Option[PensionAdvisor])

object PsaSubscription {
  implicit val formatsAddress: Format[PsaSubscription] = Json.format[PsaSubscription]
}
