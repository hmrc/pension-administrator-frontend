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

package utils

import identifiers.TypedIdentifier
import identifiers.register.VariationWorkingKnowledgeId
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, AdviserNameId}
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import models.AddressYears.UnderAYear
import models._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.countryOptions.CountryOptions
import viewmodels._

//scalastyle:off number.of.methods
class ViewPsaDetailsHelper(userAnswers: UserAnswers,
                           countryOptions: CountryOptions,
                           override val messagesApi: MessagesApi
                          )(implicit messages: Messages) extends I18nSupport {

  import ViewPsaDetailsHelper._

  private val individualDetailsSection = SuperSection(
    None,
    Seq(AnswerSection(
      None,
      Seq(
        individualDateOfBirth,
        individualNino,
        individualAddress,
        individualPreviousAddress,
        individualEmailAddress,
        individualPhoneNumber
      ).flatten
    )
    )
  )

  private def companyDetailsSection: SuperSection = {
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            companyVatNumber,
            companyPayeNumber,
            crn,
            companyUtr,
            companyAddress,
            companyPreviousAddress,
            companyEmailAddress,
            companyPhoneNumber
          ).flatten
        )
      )
    )
  }

  private def partnershipDetailsSection: SuperSection = {
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            partnershipVatNumber,
            partnershipPayeNumber,
            crn,
            partnershipUtr,
            partnershipAddress,
            partnershipPreviousAddress,
            partnershipEmailAddress,
            partnershipPhoneNumber
          ).flatten
        )
      )
    )
  }

  private def toOptionSeq[A](seq: Seq[A]): Option[Seq[A]] =
    if (seq.nonEmpty) {
      Some(seq)
    } else {
      None
    }

  private val pensionAdviserSection: Option[SuperSection] = {

    toOptionSeq(Seq(
      workingKnowledge,
      pensionAdviser,
      pensionAdviserEmail,
      pensionAdviserPhone,
      pensionAdviserAddress
    ).flatten).map { seqAnswerRow =>
      SuperSection(
        Some("pensionAdvisor.section.header"),
        Seq(
          AnswerSection(
            None,
            seqAnswerRow
          )
        ),
        getAdviserDeleteLink
      )
    }
  }

  private def getAdviserDeleteLink: Option[AddLink] = {
    userAnswers.get(AdviserNameId) match {
      case Some(adviserName) =>
        Some(AddLink(Link(
          controllers.register.adviser.routes.ConfirmDeleteAdviserController.onPageLoad().url,
          Message("adviser-delete-link", adviserName)), Some(Message("adviser-delete-link-additionalText", adviserName)))
        )
      case _ => None
    }
  }


  //Individual PSA
  private def individualDateOfBirth: Option[AnswerRow] = userAnswers.get(IndividualDateOfBirthId) map { x =>
    AnswerRow("cya.label.dob", Seq(DateHelper.formatDateWithSlash(x)), answerIsMessageKey = false,
      None)
  }

  private def individualNino: Option[AnswerRow] = userAnswers.get(IndividualNinoId) map { nino =>
    AnswerRow("common.nino", Seq(nino), answerIsMessageKey = false, None)
  }

  private def individualAddress: Option[AnswerRow] = userAnswers.get(IndividualContactAddressId) map { address =>
    AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def individualPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(IndividualAddressYearsId), userAnswers.get(IndividualPreviousAddressId)) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def individualEmailAddress: Option[AnswerRow] = userAnswers.get(IndividualContactDetailsId) map { details =>
    AnswerRow("email.label", Seq(details.email), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  private def individualPhoneNumber: Option[AnswerRow] = userAnswers.get(IndividualContactDetailsId) map { details =>
    AnswerRow("phone.label", Seq(details.phone), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  //Company PSA
  private def companyVatNumber: Option[AnswerRow] = userAnswers.get(CompanyDetailsId) flatMap (_.vatRegistrationNumber map { vat =>
    AnswerRow("vat.label", Seq(vat), answerIsMessageKey = false,
      None)
  })

  private def companyPayeNumber: Option[AnswerRow] = userAnswers.get(CompanyDetailsId) flatMap (_.payeEmployerReferenceNumber map { paye =>
    AnswerRow("paye.label", Seq(paye), answerIsMessageKey = false,
      None)
  })

  private def crn: Option[AnswerRow] = userAnswers.get(CompanyRegistrationNumberId) map { crn =>
    AnswerRow("crn.label", Seq(crn), answerIsMessageKey = false,
      None)
  }

  private def companyAddress: Option[AnswerRow] = userAnswers.get(CompanyContactAddressId) map { address =>
    AnswerRow("company.address.label", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def companyPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(CompanyAddressYearsId), userAnswers.get(CompanyPreviousAddressId)) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def companyEmailAddress: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map { details =>
    AnswerRow("company.email.label", Seq(details.email), answerIsMessageKey = false,
      Some(Link(controllers.register.company.routes.ContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  private def companyPhoneNumber: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map { details =>
    AnswerRow("company.phone.label", Seq(details.phone), answerIsMessageKey = false,
      Some(Link(controllers.register.company.routes.ContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  private def companyUtr: Option[AnswerRow] = userAnswers.get(BusinessDetailsId) flatMap (_.uniqueTaxReferenceNumber map { utr =>
    AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false, None)
  })

  //Directors
  private def directorDob(index: Int): Option[AnswerRow] = userAnswers.get(DirectorDetailsId(index)) map { details =>
    AnswerRow("cya.label.dob", Seq(details.dateOfBirth.toString), answerIsMessageKey = false,
      None)
  }

  private def directorNino(index: Int): Option[AnswerRow] = userAnswers.get(DirectorNinoId(index)) match {
    case Some(Nino.Yes(nino)) => Some(AnswerRow("common.nino", Seq(nino), answerIsMessageKey = false,
      None))

    case Some(Nino.No(_)) => Some(AnswerRow("common.nino", Seq("site.not_entered"), answerIsMessageKey = true,
      Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(UpdateMode, index).url, "site.add")))

    case _ => None
  }

  private def directorUtr(index: Int): Option[AnswerRow] = userAnswers.get(DirectorUniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) => Some(AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false,
      None))

    case Some(UniqueTaxReference.No(_)) => Some(AnswerRow("utr.label", Seq("site.not_entered"), answerIsMessageKey = true,
      Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(UpdateMode, index).url, "site.add")))

    case _ => None
  }

  private def directorAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] = userAnswers.get(DirectorAddressId(index)) map { address =>
    AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.company.directors.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url)))
  }

  private def directorPrevAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] = {
    (userAnswers.get(DirectorAddressYearsId(index)), userAnswers.get(DirectorPreviousAddressId(index))) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(UpdateMode, index).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def directorPhone(index: Int): Option[AnswerRow] = userAnswers.get(DirectorContactDetailsId(index)) map { details =>
    AnswerRow("phone.label", Seq(details.phone), answerIsMessageKey = false,
      Some(Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(UpdateMode, index).url)))
  }

  private def directorEmail(index: Int): Option[AnswerRow] = userAnswers.get(DirectorContactDetailsId(index)) map { details =>
    AnswerRow("email.label", Seq(details.email), answerIsMessageKey = false,
      Some(Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(UpdateMode, index).url)))
  }


  private def directorSection(person: Person, countryOptions: CountryOptions): AnswerSection = {
    val i = person.index
    AnswerSection(
      Some(person.name),
      Seq(directorDob(i),
        directorNino(i),
        directorUtr(i),
        directorAddress(i, countryOptions),
        directorPrevAddress(i, countryOptions),
        directorEmail(i),
        directorPhone(i)
      ).flatten
    )
  }

  private def directorsSuperSection: SuperSection = {
    val (linkText, additionalText) = userAnswers.allDirectorsAfterDelete(NormalMode).size match {
      case _ if ifAnyDirectorIncomplete => ("director-add-link-incomplete", None)
      case noOfDirectors if noOfDirectors == 1 => ("director-add-link-onlyOne", Some("director-add-link-onlyOne-additionalText"))
      case noOfDirectors if noOfDirectors == 10 => ("director-add-link-Ten", Some("director-add-link-Ten-additionalText"))
      case _ => ("director-add-link-lessThanTen", None)
    }
    SuperSection(
      Some("director.supersection.header"),
      for (person <- userAnswers.allDirectorsAfterDelete(NormalMode)) yield directorSection(person, countryOptions),
      Some(AddLink(Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(UpdateMode).url, linkText), additionalText))
    )
  }

  private def ifAnyDirectorIncomplete = userAnswers.allDirectorsAfterDelete(NormalMode).exists(!_.isComplete)

  //Partnership PSA
  private def partnershipVatNumber: Option[AnswerRow] = userAnswers.get(PartnershipVatId) match {
    case Some(Vat.Yes(vat)) => Some(AnswerRow("vat.label", Seq(vat), answerIsMessageKey = false,
      None))

    case _ => None
  }

  private def partnershipPayeNumber: Option[AnswerRow] = userAnswers.get(PartnershipPayeId) match {
    case Some(Paye.Yes(paye)) => Some(AnswerRow("paye.label", Seq(paye), answerIsMessageKey = false,
      None))
    case _ => None
  }

  private def partnershipAddress: Option[AnswerRow] = userAnswers.get(PartnershipContactAddressId) map { address =>
    AnswerRow("partnership.address.label", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(PartnershipAddressYearsId), userAnswers.get(PartnershipPreviousAddressId)) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def partnershipEmailAddress: Option[AnswerRow] = userAnswers.get(PartnershipContactDetailsId) map { details =>
    AnswerRow("partnership.email.label", Seq(details.email), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipPhoneNumber: Option[AnswerRow] = userAnswers.get(PartnershipContactDetailsId) map { details =>
    AnswerRow("partnership.phone.label", Seq(details.phone), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactDetailsController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipUtr: Option[AnswerRow] = userAnswers.get(PartnershipDetailsId) flatMap (_.uniqueTaxReferenceNumber map { utr =>
    AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false,
      None)
  })

  //Partners
  private def partnerDob(index: Int): Option[AnswerRow] = userAnswers.get(PartnerDetailsId(index)) map { details =>
    AnswerRow("cya.label.dob", Seq(details.dateOfBirth.toString), answerIsMessageKey = false,
      None)
  }

  private def partnerNino(index: Int): Option[AnswerRow] = userAnswers.get(PartnerNinoId(index)) match {
    case Some(Nino.Yes(nino)) => Some(AnswerRow("common.nino", Seq(nino), answerIsMessageKey = false,
      None))

    case Some(Nino.No(_)) => Some(AnswerRow("common.nino", Seq("site.not_entered"), answerIsMessageKey = true,
      Link(controllers.register.partnership.partners.routes.PartnerNinoController.onPageLoad(UpdateMode, index).url, "site.add")))

    case _ => None
  }

  private def partnerUtr(index: Int): Option[AnswerRow] = userAnswers.get(PartnerUniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) => Some(AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false,
      None))

    case Some(UniqueTaxReference.No(_)) => Some(AnswerRow("utr.label", Seq("site.not_entered"), answerIsMessageKey = true,
      Link(controllers.register.partnership.partners.routes.PartnerUniqueTaxReferenceController.onPageLoad(UpdateMode, index).url, "site.add")))

    case _ => None
  }

  private def partnerAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] = userAnswers.get(PartnerAddressId(index)) map { address =>
    AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url)))
  }

  private def partnerPrevAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] = {
    (userAnswers.get(PartnerAddressYearsId(index)), userAnswers.get(PartnerPreviousAddressId(index))) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.partnership.partners.routes.PartnerPreviousAddressController.onPageLoad(UpdateMode, index).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def partnerPhone(index: Int): Option[AnswerRow] = userAnswers.get(PartnerContactDetailsId(index)) map { details =>
    AnswerRow("phone.label", Seq(details.phone), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(UpdateMode, index).url)))
  }

  private def partnerEmail(index: Int): Option[AnswerRow] = userAnswers.get(PartnerContactDetailsId(index)) map { details =>
    AnswerRow("email.label", Seq(details.email), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.partners.routes.PartnerContactDetailsController.onPageLoad(UpdateMode, index).url)))
  }


  private def partnerSection(person: Person, countryOptions: CountryOptions): AnswerSection = {
    val i = person.index
    AnswerSection(
      Some(person.name),
      Seq(partnerDob(i),
        partnerNino(i),
        partnerUtr(i),
        partnerAddress(i, countryOptions),
        partnerPrevAddress(i, countryOptions),
        partnerEmail(i),
        partnerPhone(i)
      ).flatten
    )
  }

  private def partnersSuperSection: SuperSection = {
    val (linkText, additionalText) = userAnswers.allPartnersAfterDelete(UpdateMode).size match {
      case _ if ifAnyPartnerIncomplete => ("partner-add-link-incomplete", None)
      case noOfPartners if noOfPartners == 1 => ("partner-add-link-onlyOne", Some("partner-add-link-onlyOne-additionalText"))
      case noOfPartners if noOfPartners == 10 => ("partner-add-link-Ten", Some("partner-add-link-Ten-additionalText"))
      case _ => ("partner-add-link-lessThanTen", None)
    }
    SuperSection(
      Some("partner.supersection.header"),
      for (person <- userAnswers.allPartnersAfterDelete(UpdateMode)) yield partnerSection(person, countryOptions),
      Some(AddLink(Link(controllers.register.partnership.routes.AddPartnerController.onPageLoad(UpdateMode).url, linkText), additionalText))
    )
  }

  private def ifAnyPartnerIncomplete = userAnswers.allPartnersAfterDelete(NormalMode).exists(!_.isComplete)

  private def workingKnowledge: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) map { wk =>
    AnswerRow("variationWorkingKnowledge.heading", Seq(messages(if (wk) "site.yes" else "site.no")), answerIsMessageKey = false,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url)))
  }

  private def pensionAdviser: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(false) =>
      Option(userAnswers.get(AdviserNameId).fold[AnswerRow](
        AnswerRow("pensions.advisor.label", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode).url, "site.add")))) { adviserName =>
        AnswerRow("pensions.advisor.label", Seq(adviserName), answerIsMessageKey = false,
          None)
      })
    case _ => None
  }

  private def pensionAdviserEmail: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(false) =>
      Option(userAnswers.get(AdviserDetailsId).fold[AnswerRow](
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url, "site.add")))) { adviser =>
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(adviser.email), answerIsMessageKey = false,
          Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url)))
      })
    case _ => None
  }

  private def pensionAdviserPhone: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {

    case Some(false) =>
      Option(userAnswers.get(AdviserDetailsId).fold[AnswerRow](
        AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url, "site.add")))) {
        adviser =>
          AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(adviser.phone), answerIsMessageKey = false,
            Some(Link(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode).url)))
      })
    case _ => None
  }

  private def pensionAdviserAddress: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(false) =>
      Option(userAnswers.get(AdviserAddressId).fold[AnswerRow](
        AnswerRow("cya.label.address", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add")))) {
        address =>
          AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
            Some(Link(controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
      })
    case _ => None
  }

  val individualSections: Seq[SuperSection] = Seq(individualDetailsSection) ++ pensionAdviserSection.toSeq
  val companySections: Seq[SuperSection] = Seq(companyDetailsSection, directorsSuperSection) ++ pensionAdviserSection.toSeq
  val partnershipSections: Seq[SuperSection] = Seq(partnershipDetailsSection, partnersSuperSection) ++ pensionAdviserSection.toSeq
}

object ViewPsaDetailsHelper {

  def addressYearsAnswer(userAnswers: UserAnswers, id: TypedIdentifier[AddressYears]): String = {
    userAnswers.get(id) match {
      case Some(AddressYears.UnderAYear) => s"sameAddress.label.true"
      case _ => s"sameAddress.label.false"
    }
  }

  def addressAnswer(address: Address, countryOptions: CountryOptions): Seq[String] = {
    val country = countryOptions.options
      .find(_.value == address.country)
      .map(_.label)
      .getOrElse(address.country)

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postcode.map(postcode => s"$postcode,"),
      Some(country)
    ).flatten
  }
}
