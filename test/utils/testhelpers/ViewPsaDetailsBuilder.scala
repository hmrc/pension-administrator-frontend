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

import java.time.LocalDate

import models.CheckMode
import viewmodels.{AnswerRow, AnswerSection, Link, SuperSection}

object ViewPsaDetailsBuilder {

  val pensionAdviserSeqAnswers = Seq(
    AnswerRow("pensions.advisor.label", Seq("Pension Adviser"), false,
      None),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false,
      Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url))),
    AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4,", "56765,", "Country of AD"), false,
      Some(Link(controllers.register.adviser.routes.AdviserAddressController.onPageLoad(CheckMode).url))))

  val individualSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("29/03/1947"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false, None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("Has Stephen Wood been at their address for more than 12 months?", Seq("No"), false,
      None),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(Link(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url))))


  val companySeqAnswers = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      None),
    AnswerRow("paye.label", Seq("9876543210"), false,
      None),
    AnswerRow("crn.label", Seq("121414151"), false,
      None),
    AnswerRow("utr.label", Seq("1234567890"), false,
      None),
    AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.company.routes.CompanyContactAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("Has Test company name been at their address for more than 12 months?", Seq("No"), false,
      None),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("company.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url))),
    AnswerRow("company.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url))))

  val directorsSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("1950-03-29"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false,
      None),
    AnswerRow("utr.label", Seq("1234567892"), false,
      None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, 0).url)))
  )


  val partnershipSeqAnswers = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      None),
    AnswerRow("paye.label", Seq("9876543210"), false,
      None),
    AnswerRow("utr.label", Seq("121414151"), false,
      None),
    AnswerRow("partnership.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("Has Test partnership name been at their address for more than 12 months?", Seq("No"), false,
      None),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(CheckMode).url))),
    AnswerRow("partnership.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(CheckMode).url))),
    AnswerRow("partnership.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(CheckMode).url))))

  val partnersSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("1950-03-29"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false,
      None),
    AnswerRow("utr.label", Seq("1234567892"), false,
      None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerAddressController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerPreviousAddressController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(CheckMode, 0).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(CheckMode, 0).url)))
  )

  val directorsSeqAnswersWithAddLinks = Seq(
    AnswerRow("cya.label.dob", Seq(LocalDate.now().toString), false,
      None),
    AnswerRow("common.nino", Seq(""), false,
      Some(Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, 0).url, "site.add"))),
    AnswerRow("utr.label", Seq(""), false,
      Some(Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, 0).url, "site.add")))
  )

  val partnersSeqAnswersWithAddLinks = Seq(
    AnswerRow("cya.label.dob", Seq(LocalDate.now().toString), false,
      None),
    AnswerRow("common.nino", Seq(""), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerNinoController.onPageLoad(CheckMode, 0).url, "site.add"))),
    AnswerRow("utr.label", Seq(""), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerUniqueTaxReferenceController.onPageLoad(CheckMode, 0).url, "site.add")))
  )

  val pensionAdviserSuperSection = SuperSection(
    Some("pensionAdvisor.section.header"),
    Seq(
      AnswerSection(
        None, pensionAdviserSeqAnswers
        )))

  val directorsSuperSection =SuperSection(Some("director.supersection.header"),
    Seq(AnswerSection(
      Some("Director number one"),
      directorsSeqAnswers
    )),
    Some(Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "director-add-link"))
  )

  val partnersSuperSection =SuperSection(Some("partner.supersection.header"),
    Seq(AnswerSection(
      Some("Partner One"),
      partnersSeqAnswers
    )),
    Some(Link(controllers.register.partnership.routes.AddPartnerController.onPageLoad.url, "partner-add-link"))
  )

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
      directorsSuperSection,
      pensionAdviserSuperSection)

  val partnershipWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            partnershipSeqAnswers))),
      partnersSuperSection,
      pensionAdviserSuperSection)
}
