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

import base.SpecBase
import models.{CheckUpdateMode, ReferenceValue, UpdateMode}
import viewmodels._
import viewmodels.{AnswerRow, AnswerSection, SuperSection}

object ViewPsaDetailsBuilder extends SpecBase {

  val pensionAdviserSeqAnswers = Seq(
    AnswerRow("variationWorkingKnowledge.heading", Seq("No"), false,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url))),
    AnswerRow("pensions.advisor.label", Seq("Pension Adviser"), false,
      None),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false,
      Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url))),
    AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("0044-0987654232"), false,
      Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url))),
    AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4,", "56765,", "Country of AD"), false,
      Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url))))

  val pensionAdviserSeqAnswersIncomplete = Seq(
    AnswerRow("variationWorkingKnowledge.heading", Seq("No"), false,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url))),
    AnswerRow("pensions.advisor.label", Seq("site.not_entered"),answerIsMessageKey = true,
      Some(Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("cya.label.address", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add"))))

  def individualSeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("cya.label.dob", Seq("29/03/1947"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false, None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if(noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), false,
        Some(Link(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.individual.routes.IndividualEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.individual.routes.IndividualPhoneController.onPageLoad(UpdateMode).url))))

  def companySeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      None),
    AnswerRow("paye.label", Seq("9876543210"), false,
      None),
    AnswerRow("crn.label", Seq("121414151"), false,
      None),
    AnswerRow("utr.label", Seq("1234567890"), false,
      None),
    AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if(noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("company.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.company.routes.CompanyEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("company.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.routes.CompanyPhoneController.onPageLoad(UpdateMode).url))))

  val directorsSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("1950-03-29"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false,
      None),
    AnswerRow("utr.label", Seq("1234567892"), false,
      None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.company.directors.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      None),
    AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorEmailController.onPageLoad(UpdateMode, 0).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorPhoneController.onPageLoad(UpdateMode, 0).url)))
  )


  def partnershipSeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("vat.label", Seq("12345678"), false,
      None),
    AnswerRow("paye.label", Seq("9876543210"), false,
      None),
    AnswerRow("utr.label", Seq("121414151"), false,
      None),
    AnswerRow("partnership.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if(noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("partnership.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("partnership.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(UpdateMode).url))))

  val partnersSeqAnswers = Seq(
    AnswerRow("cya.label.dob", Seq("1950-03-29"), false,
      None),
    AnswerRow("common.nino", Seq("AA999999A"), false,
      None),
    AnswerRow("utr.label", Seq("1234567892"), false,
      None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
      None),
    AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(UpdateMode, 0).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(UpdateMode, 0).url)))
  )

  val directorsSeqAnswersWithAddLinks = Seq(
    AnswerRow("cya.label.dob", Seq("2019-10-23"), false,
      None),
    AnswerRow("common.nino", Seq("site.not_entered"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorEnterNINOController.onPageLoad(UpdateMode, 0).url, "site.add"))),
    AnswerRow("utr.label", Seq("site.not_entered"), false,
      Some(Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(UpdateMode, 0).url, "site.add"))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
      Some(Link(controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(UpdateMode, 0).url, "site.add")))
  )

  val partnersSeqAnswersWithAddLinks = Seq(
    AnswerRow("cya.label.dob", Seq(LocalDate.now().toString), false,
      None),
    AnswerRow("common.nino", Seq("site.not_entered"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerNinoController.onPageLoad(UpdateMode, 0).url, "site.add"))),
    AnswerRow("utr.label", Seq("site.not_entered"), false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerUniqueTaxReferenceController.onPageLoad(UpdateMode, 0).url, "site.add"))),
    AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
      Some(Link(controllers.register.partnership.partners.routes.PartnerPreviousAddressController.onPageLoad(UpdateMode, 0).url, "site.add")))
  )

  val pensionAdviserSuperSection = SuperSection(
    Some("pensionAdvisor.section.header"),
    Seq(
      AnswerSection(
        None, pensionAdviserSeqAnswers
        )),
    Some(AddLink(Link(
      controllers.register.adviser.routes.ConfirmDeleteAdviserController.onPageLoad().url,
      Message("adviser-delete-link", "Pension Adviser")),
      Some(Message("adviser-delete-link-additionalText", "Pension Adviser")))
    )
  )

  val pensionAdviserSuperSectionWithAddLinks = SuperSection(
    Some("pensionAdvisor.section.header"),
    Seq(
      AnswerSection(
        None, pensionAdviserSeqAnswersIncomplete
      )),
    None
  )

  val directorsSuperSection =SuperSection(Some("director.supersection.header"),
    Seq(AnswerSection(
      Some("Director one"),
      directorsSeqAnswers
    )),
    Some(AddLink(
      Link(
        controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(UpdateMode).url,
      "director-add-link-onlyOne"
      ),
      Some("director-add-link-onlyOne-additionalText")
    ))
  )

  val partnersSuperSection =SuperSection(Some("partner.supersection.header"),
    Seq(AnswerSection(
      Some("Partner One"),
      partnersSeqAnswers
    )),
    Some(AddLink(
      Link(controllers.register.partnership.routes.AddPartnerController.onPageLoad(UpdateMode).url,
        "partner-add-link-onlyOne"
      ),
      Some("partner-add-link-onlyOne-additionalText")
    ))
  )

  val individualWithChangeLinks: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          individualSeqAnswers()))),
    pensionAdviserSuperSection
  )

  val companyWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            companySeqAnswers()))),
      directorsSuperSection,
      pensionAdviserSuperSectionWithAddLinks)

  val partnershipWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            partnershipSeqAnswers()))),
      partnersSuperSection,
      pensionAdviserSuperSection)
}
