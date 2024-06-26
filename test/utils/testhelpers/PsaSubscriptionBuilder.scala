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

package utils.testhelpers

import models.PsaSubscription._

import java.time.LocalDate

object PsaSubscriptionBuilder {

  val customerId = CustomerIdentification("Individual", Some("NINO"), Some("AA999999A"), isOverseasCustomer = true)
  val orgCustomerId = CustomerIdentification("Company", Some("UTR"), Some("121414151"), isOverseasCustomer = true)
  val individual = IndividualDetailType(Some("Mr"), "Stephen", None, "Wood", LocalDate.parse("1947-03-29"))

  val address = CorrespondenceAddress("Telford1", "Telford2",Some("Telford3"), Some("Telford4"), "GB", Some("TF3 4ER"))
  val director1Address = CorrespondenceAddress("addressline1", "addressline2",Some("addressline3"), Some("addressline4"), "GB", Some("B5 9EX"))
  val director1PrevAddress = CorrespondenceAddress("line1", "line2",Some("line3"), Some("line4"), "AD", Some("567253"))
  val director1Contact = PsaContactDetails("0044-09876542312", Some("abc@hmrc.gsi.gov.uk"))

  val director2Address = CorrespondenceAddress("fgfdgdfgfd", "dfgfdgdfg",Some("fdrtetegfdgdg"), Some("dfgfdgdfg"), "AD", Some("56546"))
  val director2PrevAddress = CorrespondenceAddress("werrertqe", "ereretfdg",Some("asafafg"), Some("fgdgdasdf"), "AD", Some("23424"))
  val director2Contact = PsaContactDetails("0044-09876542334", Some("aaa@gmail.com"))

  val contactDetails = PsaContactDetails("0044-09876542312", Some("aaa@aa.com"))
  val previousAddress = CorrespondenceAddress("London1", "London2", Some("London3"), Some("London4"), "GB", Some("LN12 4DC"))

  val psaAddress = CorrespondenceAddress("addline1", "addline2", Some("addline3"), Some("addline4 "), "AD", Some("56765"))
  val psaContactDetails = PsaContactDetails("0044-0987654232", Some("aaa@yahoo.com"))
  val pensionsAdvisor = PensionAdvisor("Pension Advisor", psaAddress, Some(psaContactDetails))

  val company = OrganisationOrPartner(name = "Test company name", crn = Some("1234567890"), vatRegistration = Some("12345678"), paye = Some("9876543210"))
  val partnership = OrganisationOrPartner(name = "Test partnership name", crn = None, vatRegistration = Some("12345678"),
    paye = Some("9876543210"))

  val director1 = DirectorOrPartner("Director", Some("Mr"), "abcdef", Some("dfgdsfff"), "dfgfdgfdg", LocalDate.parse("1950-03-29"),
    Some("AA999999A"), Some("1234567892"), isSameAddressForLast12Months = true, Some(director1PrevAddress),
    Some(CorrespondenceDetails(director1Address, Some(director1Contact))))
  val director2 = DirectorOrPartner("Director", Some("Mr"), "sdfdff", Some("sdfdsfsdf"), "dfdsfsf", LocalDate.parse("1950-07-29"),
    Some("AA999999A"), Some("7897700000"), isSameAddressForLast12Months = true, Some(director2PrevAddress),
    Some(CorrespondenceDetails(director2Address, Some(director2Contact))))

  val psaSubscriptionIndividual = PsaSubscription(isSuspended = false, customerId, None, Some(individual), address, contactDetails,
  isSameAddressForLast12Months = true, Some(previousAddress), None, Some(pensionsAdvisor))

  val psaSubscriptionCompany = PsaSubscription(isSuspended = false, orgCustomerId, Some(company), None, address, contactDetails,
    isSameAddressForLast12Months = true, Some(previousAddress), Some(Seq(director1, director2)), Some(pensionsAdvisor))


  val psaSubscriptionPartnership = PsaSubscription(isSuspended = false, orgCustomerId, Some(partnership), None, address, contactDetails,
    isSameAddressForLast12Months = true, Some(previousAddress),
    Some(Seq(director1.copy(isDirectorOrPartner="Partner"), director2.copy(isDirectorOrPartner="Partner"))),
    Some(pensionsAdvisor))

  val psaSubscriptionMinimum = PsaSubscription(isSuspended = false, customerId, None, None, address, contactDetails, isSameAddressForLast12Months = false,
    None, None, None)


}
