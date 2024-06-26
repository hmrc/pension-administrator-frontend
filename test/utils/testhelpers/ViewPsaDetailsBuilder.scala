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

import base.SpecBase
import controllers.register.company.directors.routes._
import controllers.register.partnership.partners.routes._
import models.UpdateMode
import viewmodels._

import java.time.LocalDate

object ViewPsaDetailsBuilder extends SpecBase {

  val pensionAdviserSeqAnswers = Seq(
    AnswerRow("variationWorkingKnowledge.heading", Seq("site.no"), true,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url))),
    AnswerRow("adviserName.heading", Seq("Pension Adviser"), false,
      None),
    AnswerRow("cya.label.address", Seq("addline1,", "addline2,", "addline3,", "addline4,", "56765,", "Country of AD"), false,
      Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false,
      Some(Link(controllers.register.adviser.routes.AdviserEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("0044-0987654232"), false,
      Some(Link(controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(UpdateMode).url))))

  val pensionAdviserSeqAnswersIncomplete = Seq(
    AnswerRow("variationWorkingKnowledge.heading", Seq("site.no"), true,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url))),
    AnswerRow("adviserName.heading", Seq("site.not_entered"), answerIsMessageKey = true,
      Some(Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("cya.label.address", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserEmailController.onPageLoad(UpdateMode).url, "site.add"))),
    AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("site.not_entered"), true,
      Some(Link(controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(UpdateMode).url, "site.add"))))

  def individualSeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("cya.label.dob", Seq("29/03/1947"), false,
      None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), false,
        Some(Link(controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.individual.routes.IndividualEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.individual.routes.IndividualPhoneController.onPageLoad(UpdateMode).url))))

  def individualContactOnlySeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("cya.label.adminId", Seq("A2100005"), false, None),
    AnswerRow("cya.label.dob", Seq("29/03/1947"), false, None),
    AnswerRow("cya.label.address", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), false,
        Some(Link(controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
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
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("company.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.company.routes.CompanyEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("company.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.routes.CompanyPhoneController.onPageLoad(UpdateMode).url))))

  def companyContactOnlySeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("cya.label.adminId", Seq("A2100005"), false, None),
    AnswerRow("utr.label", Seq("1234567890"), false,
      None),
    AnswerRow("company.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("company.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.company.routes.CompanyEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("company.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.company.routes.CompanyPhoneController.onPageLoad(UpdateMode).url))))

  val directorsSeqAnswers = Seq(
    AnswerRow(
      label = "cya.label.dob",
      answer = Seq("1950-03-29"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "directorNino.checkYourAnswersLabel",
      answer = Seq("Yes"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "directorUniqueTaxReference.checkYourAnswersLabel",
      answer = Seq("Yes"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "utr.label",
      answer = Seq("1234567892"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "cya.label.address",
      answer = Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "common.previousAddress.checkyouranswers",
      answer = Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "email.label",
      answer = Seq("abc@hmrc.gsi.gov.uk"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(DirectorEmailController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "phone.label",
      answer = Seq("0044-09876542312"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(DirectorPhoneController.onPageLoad(UpdateMode, 0).url))
    )
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
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("partnership.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("partnership.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(UpdateMode).url))))

  def partnershipContactOnlySeqAnswers(noPrevAddr: Boolean = false) = Seq(
    AnswerRow("cya.label.adminId", Seq("A2100005"), false, None),
    AnswerRow("utr.label", Seq("121414151"), false,
      None),
    AnswerRow("partnership.address.label", Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url))),
    if (noPrevAddr) {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), true,
        Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))
    } else {
      AnswerRow("common.previousAddress.checkyouranswers", Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"), false,
        None)
    },
    AnswerRow("partnership.email.label", Seq("aaa@aa.com"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(UpdateMode).url))),
    AnswerRow("partnership.phone.label", Seq("0044-09876542312"), false,
      Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(UpdateMode).url))))


  val partnersSeqAnswers = Seq(
    AnswerRow(
      label = "cya.label.dob",
      answer = Seq("1950-03-29"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "partnerNino.checkYourAnswersLabel",
      answer = Seq("Yes"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "partnerUniqueTaxReference.checkYourAnswersLabel",
      answer = Seq("Yes"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "utr.label",
      answer = Seq("1234567892"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "cya.label.address",
      answer = Seq("Telford1,", "Telford2,", "Telford3,", "Telford4,", "TF3 4ER,", "Country of GB"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "common.previousAddress.checkyouranswers",
      answer = Seq("London1,", "London2,", "London3,", "London4,", "LN12 4DC,", "Country of GB"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "email.label",
      answer = Seq("abc@hmrc.gsi.gov.uk"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerEmailController.onPageLoad(UpdateMode, 0).url))
    ),
    AnswerRow(
      label = "phone.label",
      answer = Seq("0044-09876542312"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerPhoneController.onPageLoad(UpdateMode, 0).url))
    )
  )

  val directorsSeqAnswersWithAddLinks = Seq(
    AnswerRow(
      label = "cya.label.dob",
      answer = Seq("2019-10-23"),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "directorNino.checkYourAnswersLabel",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "directorUniqueTaxReference.checkYourAnswersLabel",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(UpdateMode, 0).url, "site.add")),
      visuallyHiddenText = None
    ),
    AnswerRow(
      label = "common.previousAddress.checkyouranswers",
      answer = Seq("Not entered"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(DirectorPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "cya.label.address",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "email.label",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(DirectorEmailController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "phone.label",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(DirectorPhoneController.onPageLoad(UpdateMode, 0).url, "site.add"))
    )
  )

  val partnersSeqAnswersWithAddLinks = Seq(
    AnswerRow(
      label = "cya.label.dob",
      answer = Seq(LocalDate.now().toString),
      answerIsMessageKey = false,
      changeUrl = None
    ),
    AnswerRow(
      label = "partnerNino.checkYourAnswersLabel",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "partnerUniqueTaxReference.checkYourAnswersLabel",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(UpdateMode, 0).url, "site.add")),
      visuallyHiddenText = None
    ),
    AnswerRow(
      label = "common.previousAddress.checkyouranswers",
      answer = Seq("Not entered"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(PartnerPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "cya.label.address",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "email.label",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerEmailController.onPageLoad(UpdateMode, 0).url, "site.add"))
    ),
    AnswerRow(
      label = "phone.label",
      answer = Seq("Not entered"),
      answerIsMessageKey = false,
      changeUrl = Some(Link(PartnerPhoneController.onPageLoad(UpdateMode, 0).url, "site.add"))
    )
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

  val directorsSuperSection = SuperSection(Some("director.supersection.header"),
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

  val partnersSuperSection = SuperSection(Some("partner.supersection.header"),
    Seq(AnswerSection(
      Some("Partner One"),
      partnersSeqAnswers
    )),
    Some(AddLink(
      Link(controllers.register.partnership.routes.AddPartnerController.onPageLoad(UpdateMode).url,
        "partner-add-link-lessThanTwo"
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

  val individualContactOnlyWithChangeLinks: Seq[SuperSection] = Seq(
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          individualContactOnlySeqAnswers())))
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

  val companyContactOnlyWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            companyContactOnlySeqAnswers()))))

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

  val partnershipContactOnlyWithChangeLinks =
    Seq(
      SuperSection(
        None,
        Seq(
          AnswerSection(
            None,
            partnershipContactOnlySeqAnswers()))))
}
