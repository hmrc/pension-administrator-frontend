/*
 * Copyright 2019 HM Revenue & Customs
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

import models.CheckMode
import viewmodels.{AnswerRow, AnswerSection, SuperSection}

object ViewPsaDetailsBuilder {

  val pensionAdviserSeqAnswers = Seq(
    AnswerRow("pensions.advisor.label", Seq("Pension Adviser"), false,
      Some(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false,
      Some(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4,", "56765,", "Country of AD"), false,
      Some(controllers.register.adviser.routes.AdviserAddressController.onPageLoad(CheckMode).url)))

  val individualSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("29/03/1947"), false,
      Some(controllers.register.individual.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url)),
    AnswerRow("common.nino", Seq("AA999999A"), false, None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(controllers.register.individual.routes.IndividualContactAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("Has Stephen Wood been at their address for more than 12 months?", Seq("No"), false,
      Some(controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url)),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("email.label", Seq("aaa@aa.com"), false,
      Some(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url)))


  val companySeqAnswers = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      Some(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("paye.label", Seq("9876543210"), false,
      Some(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("crn.label", Seq("121414151"), false,
      Some(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)),
    AnswerRow("utr.label", Seq("1234567890"), false,
      Some(controllers.register.company.routes.CompanyBusinessDetailsController.onPageLoad.url)),
    AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(controllers.register.company.routes.CompanyContactAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("Has Test company name been at their address for more than 12 months?", Seq("No"), false,
      Some(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("company.email.label", Seq("aaa@aa.com"), false,
      Some(controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("company.phone.label", Seq("0044-09876542312"), false,
      Some(controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url)))


  val partnershipSeqAnswers = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      Some(controllers.register.partnership.routes.PartnershipVatController.onPageLoad(CheckMode).url)),
    AnswerRow("paye.label", Seq("9876543210"), false,
      Some(controllers.register.partnership.routes.PartnershipPayeController.onPageLoad(CheckMode).url)),
    AnswerRow("utr.label", Seq("121414151"), false,
      Some(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad.url)),
    AnswerRow("partnership.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(controllers.register.partnership.routes.PartnershipContactAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("Has Test partnership name been at their address for more than 12 months?", Seq("No"), false,
      Some(controllers.register.partnership.routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(CheckMode).url)),
    AnswerRow("partnership.email.label", Seq("aaa@aa.com"), false,
      Some(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(CheckMode).url)),
    AnswerRow("partnership.phone.label", Seq("0044-09876542312"), false,
      Some(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(CheckMode).url)))

  val pensionAdviserSuperSection = SuperSection(
    Some("pensionAdvisor.section.header"),
    Seq(
      AnswerSection(
        None, pensionAdviserSeqAnswers
        )))

  val individualWithChangeLinks: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          individualSeqAnswers))),
    pensionAdviserSuperSection
  )

  val companyWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            companySeqAnswers))),
      pensionAdviserSuperSection)

  val partnershipWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            partnershipSeqAnswers))),
      pensionAdviserSuperSection)
}
