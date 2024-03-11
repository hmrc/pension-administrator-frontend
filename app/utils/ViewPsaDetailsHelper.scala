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

package utils

import controllers.register.company.directors.routes._
import controllers.register.company.routes._
import controllers.register.partnership.partners.routes._
import identifiers.TypedIdentifier
import identifiers.register.adviser.{AdviserAddressId, AdviserEmailId, AdviserNameId, AdviserPhoneId}
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import identifiers.register.{BusinessUTRId, EnterPAYEId, EnterVATId, VariationWorkingKnowledgeId}
import models.AddressYears.UnderAYear
import models._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels._

// scalastyle:off
class ViewPsaDetailsHelper(userAnswers: UserAnswers,
                           countryOptions: CountryOptions
                          )(implicit messages: Messages) {

  private val individualDetailsSection = SuperSection(
    None,
    Seq(AnswerSection(
      None,
      Seq(
        individualDateOfBirth,
        individualAddress,
        individualPreviousAddress,
        individualEmailAddress,
        individualPhoneNumber
      ).flatten
    )
    )
  )

  private def individualDetailsContactOnlySection(psaId: String) = SuperSection(
    None,
    Seq(AnswerSection(
      None,
      Seq(
        psaIdAnswerRow(psaId),
        individualDateOfBirth,
        individualContactAddress,
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

  private def companyDetailsContactOnlySection(psaId: String): SuperSection = {
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            psaIdAnswerRow(psaId),
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

  private def partnershipDetailsContactOnlySection(psaId: String): SuperSection = {
    SuperSection(
      None,
      Seq(
        AnswerSection(
          None,
          Seq(
            psaIdAnswerRow(psaId),
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
      pensionAdviserAddress,
      pensionAdviserEmail,
      pensionAdviserPhone
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

  private def psaIdAnswerRow(psaId: String): Option[AnswerRow] =
    Some(AnswerRow("cya.label.adminId", Seq(psaId), answerIsMessageKey = false, None))

  //Individual PSA

  private def individualDateOfBirth: Option[AnswerRow] = userAnswers.get(IndividualDateOfBirthId) map { x =>
    AnswerRow("cya.label.dob", Seq(DateHelper.formatDateWithSlash(x)), answerIsMessageKey = false,
      None)
  }

  private def individualAddress: Option[AnswerRow] = userAnswers.get(IndividualContactAddressId) map { address =>
    AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def individualContactAddress: Option[AnswerRow] = userAnswers.get(IndividualContactAddressId) map { address =>
    AnswerRow("cya.label.address", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def individualPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(IndividualAddressYearsId), userAnswers.get(IndividualPreviousAddressId)) match {
      case (Some(UnderAYear), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def individualEmailAddress: Option[AnswerRow] = userAnswers.get(IndividualEmailId) map { details =>
    AnswerRow("email.label", Seq(details), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualEmailController.onPageLoad(UpdateMode).url)))
  }

  private def individualPhoneNumber: Option[AnswerRow] = userAnswers.get(IndividualPhoneId) map { details =>
    AnswerRow("phone.label", Seq(details), answerIsMessageKey = false,
      Some(Link(controllers.register.individual.routes.IndividualPhoneController.onPageLoad(UpdateMode).url)))
  }

  //Company PSA
  private def companyVatNumber: Option[AnswerRow] = userAnswers.get(EnterVATId) map { vat =>
    AnswerRow("vat.label", Seq(vat), answerIsMessageKey = false,
      None)
  }

  private def companyPayeNumber: Option[AnswerRow] = userAnswers.get(EnterPAYEId) map { paye =>
    AnswerRow("paye.label", Seq(paye), answerIsMessageKey = false,
      None)
  }

  private def crn: Option[AnswerRow] = userAnswers.get(CompanyRegistrationNumberId) map { crn =>
    AnswerRow("crn.label", Seq(crn), answerIsMessageKey = false,
      None)
  }

  private def companyAddress: Option[AnswerRow] = userAnswers.get(CompanyContactAddressId) map { address =>
    AnswerRow("company.address.label", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def companyPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(CompanyConfirmPreviousAddressId), userAnswers.get(CompanyPreviousAddressId)) match {
      case (Some(false), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(CompanyPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def companyEmailAddress: Option[AnswerRow] = userAnswers.get(CompanyEmailId) map { email =>
    AnswerRow("company.email.label", Seq(email), answerIsMessageKey = false,
      Some(Link(CompanyEmailController.onPageLoad(UpdateMode).url)))
  }

  private def companyPhoneNumber: Option[AnswerRow] = userAnswers.get(CompanyPhoneId) map { phone =>
    AnswerRow("company.phone.label", Seq(phone), answerIsMessageKey = false,
      Some(Link(CompanyPhoneController.onPageLoad(UpdateMode).url)))
  }

  private def companyUtr: Option[AnswerRow] = userAnswers.get(BusinessUTRId) map { utr =>
    AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false, None)
  }

  //Directors
  private def directorDob(index: Int): Option[AnswerRow] =
    userAnswers.get(DirectorDOBId(index)) map {
      dob =>
        AnswerRow(
          label = "cya.label.dob",
          answer = Seq(dob.toString),
          answerIsMessageKey = false,
          changeUrl = None
        )
    }

  private def directorHasNino(index: Index): Option[AnswerRow] =
    Some((
      userAnswers.get(HasDirectorNINOId(index)),
      userAnswers.get(DirectorEnterNINOId(index))
    ) match {
      case (Some(answer), _) =>
        AnswerRow(
          label = "directorNino.checkYourAnswersLabel",
          answer = Seq(messages(s"${if (answer) "site.yes" else "site.no"}")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case (_, Some(_)) =>
        AnswerRow(
          label = "directorNino.checkYourAnswersLabel",
          answer = Seq(messages("site.yes")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "directorNino.checkYourAnswersLabel",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def directorNoNinoReason(index: Index): Option[AnswerRow] =
    Some(userAnswers.get(DirectorNoNINOReasonId(index)) match {
      case Some(reason) =>
        AnswerRow(
          label = "directorNino.checkYourAnswersLabel.reason",
          answer = Seq(reason),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorNoNINOReasonController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "directorNino.checkYourAnswersLabel.reason",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorNoNINOReasonController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def directorHasUtr(index: Index): Option[AnswerRow] =
    Some((
      userAnswers.get(HasDirectorUTRId(index)),
      userAnswers.get(DirectorEnterUTRId(index))
    ) match {
      case (Some(answer), _) =>
        AnswerRow(
          label = "directorUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages(s"${if (answer) "site.yes" else "site.no"}")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case (_, Some(_)) =>
        AnswerRow(
          label = "directorUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages("site.yes")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "directorUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def directorNoUtrReason(index: Index): Option[AnswerRow] =
    Some(userAnswers.get(DirectorNoUTRReasonId(index)) match {
      case Some(reason) =>
        AnswerRow(
          label = "directorUniqueTaxReference.checkYourAnswersLabel.reason",
          answer = Seq(reason),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorNoUTRReasonController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "directorUniqueTaxReference.checkYourAnswersLabel.reason",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorNoUTRReasonController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def directorUtr(index: Int): Option[AnswerRow] =
    (
      userAnswers.get(HasDirectorUTRId(index)),
      userAnswers.get(DirectorEnterUTRId(index))
    ) match {
      case (_, Some(ReferenceValue(utr, false))) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(utr),
          answerIsMessageKey = false,
          changeUrl = None
        ))
      case (_, Some(ReferenceValue(utr, true))) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(utr),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorEnterUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        ))
      case (Some(false), _) =>
        directorNoUtrReason(index)
      case (Some(true), None) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorEnterUTRController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        ))
      case _ =>
        None
    }

  private def directorAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] =
    Some(userAnswers.get(DirectorAddressId(index)) match {
      case Some(address) =>
        AnswerRow(
          label = "cya.label.address",
          answer = addressAnswer(address, countryOptions),
          answerIsMessageKey = false,
          changeUrl = Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "cya.label.address",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = true,
          changeUrl = Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def directorPrevAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] =
    (
      userAnswers.get(DirectorAddressYearsId(index)),
      userAnswers.get(DirectorPreviousAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), None) =>
        Some(AnswerRow(
          label = "common.previousAddress.checkyouranswers",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = true,
          changeUrl = Some(Link(DirectorPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url, "site.add"))
        ))
      case (_, Some(address)) =>
        Some(AnswerRow(
          label = "common.previousAddress.checkyouranswers",
          answer = addressAnswer(address, countryOptions),
          answerIsMessageKey = false,
          changeUrl = None
        ))
      case _ =>
        None
    }

  private def directorPhone(index: Int): Option[AnswerRow] = {
    Some(userAnswers.get(DirectorPhoneId(index)) match {
      case Some(phone) =>
        AnswerRow(
          label = "phone.label",
          answer = Seq(phone),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorPhoneController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "phone.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorPhoneController.onPageLoad(UpdateMode, index).url, "site.add"))
        )
    })
  }

  private def directorEmail(index: Int): Option[AnswerRow] = {
    Some(userAnswers.get(DirectorEmailId(index)) match {
      case Some(email) =>
        AnswerRow(
          label = "email.label",
          answer = Seq(email),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorEmailController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "email.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(DirectorEmailController.onPageLoad(UpdateMode, index).url, "site.add"))
        )
    })
  }

  private def directorSection(person: Person, countryOptions: CountryOptions): AnswerSection = {
    val i = person.index
    AnswerSection(
      headingKey = Some(person.name),
      rows = Seq(
        directorDob(i),
        directorHasNino(i),
        directorHasUtr(i),
        directorUtr(i),
        directorAddress(i, countryOptions),
        directorPrevAddress(i, countryOptions),
        directorEmail(i),
        directorPhone(i)
      ).flatten
    )
  }

  private def directorsSuperSection: SuperSection = {
    val (linkText, additionalText) =
      userAnswers.allDirectorsAfterDelete(NormalMode).size match {
        case _ if ifAnyDirectorIncomplete =>
          ("director-add-link-incomplete", None)
        case noOfDirectors if noOfDirectors == 1 =>
          ("director-add-link-onlyOne", Some("director-add-link-onlyOne-additionalText"))
        case noOfDirectors if noOfDirectors == 10 =>
          ("director-add-link-Ten", Some("director-add-link-Ten-additionalText"))
        case _ =>
          ("director-add-link-lessThanTen", None)
      }
    SuperSection(
      headingKey = Some("director.supersection.header"),
      sections = for {
        person <- userAnswers.allDirectorsAfterDelete(NormalMode)
      } yield directorSection(person, countryOptions),
      addLink = Some(AddLink(Link(AddCompanyDirectorsController.onPageLoad(UpdateMode).url, linkText), additionalText))
    )
  }

  private def ifAnyDirectorIncomplete: Boolean =
    userAnswers.allDirectorsAfterDelete(NormalMode).exists(!_.isComplete)

  //Partnership PSA
  private def partnershipVatNumber: Option[AnswerRow] = userAnswers.get(EnterVATId) match {
    case Some(vat) => Some(AnswerRow("vat.label", Seq(vat), answerIsMessageKey = false,
      None))

    case _ => None
  }

  private def partnershipPayeNumber: Option[AnswerRow] = userAnswers.get(EnterPAYEId) map { paye =>
    AnswerRow("paye.label", Seq(paye), answerIsMessageKey = false,
      None)
  }

  private def partnershipAddress: Option[AnswerRow] = userAnswers.get(PartnershipContactAddressId) map { address =>
    AnswerRow("partnership.address.label", addressAnswer(address, countryOptions), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipPreviousAddress: Option[AnswerRow] = {
    (userAnswers.get(PartnershipConfirmPreviousAddressId), userAnswers.get(PartnershipPreviousAddressId)) match {
      case (Some(false), None) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode).url, "site.add"))))
      case (_, Some(address)) =>
        Some(AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), answerIsMessageKey = false,
          None))
      case _ => None
    }
  }

  private def partnershipEmailAddress: Option[AnswerRow] = userAnswers.get(PartnershipEmailId) map { details =>
    AnswerRow("partnership.email.label", Seq(details), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipPhoneNumber: Option[AnswerRow] = userAnswers.get(PartnershipPhoneId) map { details =>
    AnswerRow("partnership.phone.label", Seq(details), answerIsMessageKey = false,
      Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(UpdateMode).url)))
  }

  private def partnershipUtr: Option[AnswerRow] = userAnswers.get(BusinessUTRId) map { utr =>
    AnswerRow("utr.label", Seq(utr), answerIsMessageKey = false,
      None)
  }

  //Partners
  private def partnerDob(index: Int): Option[AnswerRow] = userAnswers.get(PartnerDOBId(index)) map { dateOfBirth =>
    AnswerRow("cya.label.dob", Seq(dateOfBirth.toString), answerIsMessageKey = false,
      None)
  }

  private def partnerHasNino(index: Index): Option[AnswerRow] =
    Some((
      userAnswers.get(HasPartnerNINOId(index)),
      userAnswers.get(PartnerEnterNINOId(index))
    ) match {
      case (Some(answer), _) =>
        AnswerRow(
          label = "partnerNino.checkYourAnswersLabel",
          answer = Seq(messages(s"${if (answer) "site.yes" else "site.no"}")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case (_, Some(_)) =>
        AnswerRow(
          label = "partnerNino.checkYourAnswersLabel",
          answer = Seq(messages("site.yes")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "partnerNino.checkYourAnswersLabel",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerNINOController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def partnerNoNinoReason(index: Index): Option[AnswerRow] =
    Some(userAnswers.get(PartnerNoNINOReasonId(index)) match {
      case Some(reason) =>
        AnswerRow(
          label = "partnerNino.checkYourAnswersLabel.reason",
          answer = Seq(reason),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerNoNINOReasonController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "partnerNino.checkYourAnswersLabel.reason",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerNoNINOReasonController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def partnerHasUtr(index: Index): Option[AnswerRow] =
    Some((
      userAnswers.get(HasPartnerUTRId(index)),
      userAnswers.get(PartnerEnterUTRId(index))
    ) match {
      case (Some(answer), _) =>
        AnswerRow(
          label = "partnerUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages(s"${if (answer) "site.yes" else "site.no"}")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case (_, Some(_)) =>
        AnswerRow(
          label = "partnerUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages("site.yes")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "partnerUniqueTaxReference.checkYourAnswersLabel",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(HasPartnerUTRController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def partnerNoUtrReason(index: Index): Option[AnswerRow] =
    Some(userAnswers.get(PartnerNoUTRReasonId(index)) match {
      case Some(reason) =>
        AnswerRow(
          label = "partnerUniqueTaxReference.checkYourAnswersLabel.reason",
          answer = Seq(reason),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerNoUTRReasonController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        )
      case _ =>
        AnswerRow(
          label = "partnerUniqueTaxReference.checkYourAnswersLabel.reason",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerNoUTRReasonController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def partnerUtr(index: Int): Option[AnswerRow] =
    (
      userAnswers.get(HasPartnerUTRId(index)),
      userAnswers.get(PartnerEnterUTRId(index))
    ) match {
      case (_, Some(ReferenceValue(utr, false))) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(utr),
          answerIsMessageKey = false,
          changeUrl = None
        ))
      case (_, Some(ReferenceValue(utr, true))) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(utr),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerEnterUTRController.onPageLoad(UpdateMode, index).url)),
          visuallyHiddenText = None
        ))
      case (Some(false), _) =>
        partnerNoUtrReason(index)
      case (Some(true), None) =>
        Some(AnswerRow(
          label = "utr.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerEnterUTRController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        ))
      case _ =>
        None
    }

  private def partnerAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] =
    Some(userAnswers.get(PartnerAddressId(index)) match {
      case Some(address) =>
        AnswerRow(
          label = "cya.label.address",
          answer = addressAnswer(address, countryOptions),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "cya.label.address",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = true,
          changeUrl = Some(Link(PartnerAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url, "site.add")),
          visuallyHiddenText = None
        )
    })

  private def partnerPrevAddress(index: Int, countryOptions: CountryOptions): Option[AnswerRow] =
    (
      userAnswers.get(PartnerAddressYearsId(index)),
      userAnswers.get(PartnerPreviousAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), None) =>
        Some(AnswerRow(
          label = "common.previousAddress.checkyouranswers",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = true,
          changeUrl = Some(Link(PartnerPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode, index).url, "site.add"))
        ))
      case (_, Some(address)) =>
        Some(AnswerRow(
          label = "common.previousAddress.checkyouranswers",
          answer = addressAnswer(address, countryOptions),
          answerIsMessageKey = false,
          changeUrl = None
        ))
      case _ =>
        None
    }

  private def partnerPhone(index: Int): Option[AnswerRow] = {
    Some(userAnswers.get(PartnerPhoneId(index)) match {
      case Some(phone) =>
        AnswerRow(
          label = "phone.label",
          answer = Seq(phone),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerPhoneController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "phone.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerPhoneController.onPageLoad(UpdateMode, index).url, "site.add"))
        )
    })
  }

  private def partnerEmail(index: Int): Option[AnswerRow] = {
    Some(userAnswers.get(PartnerEmailId(index)) match {
      case Some(email) =>
        AnswerRow(
          label = "email.label",
          answer = Seq(email),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerEmailController.onPageLoad(UpdateMode, index).url))
        )
      case _ =>
        AnswerRow(
          label = "email.label",
          answer = Seq(messages("site.not_entered")),
          answerIsMessageKey = false,
          changeUrl = Some(Link(PartnerEmailController.onPageLoad(UpdateMode, index).url, "site.add"))
        )
    })
  }


  private def partnerSection(person: Person, countryOptions: CountryOptions): AnswerSection = {
    val i = person.index
    AnswerSection(
      Some(person.name),
      Seq(partnerDob(i),
        partnerHasNino(i),
        partnerHasUtr(i),
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
      case noOfPartners if noOfPartners <= 2 => ("partner-add-link-lessThanTwo",
        if (noOfPartners == 1) Some("partner-add-link-onlyOne-additionalText") else Some("partner-add-link-onlyTwo-additionalText"))
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
    AnswerRow("variationWorkingKnowledge.heading", Seq(if (wk) "site.yes" else "site.no"), answerIsMessageKey = true,
      Some(Link(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url)))
  }

  private def pensionAdviser: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(false) =>
      Option(userAnswers.get(AdviserNameId).fold[AnswerRow](
        AnswerRow("adviserName.heading", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode).url, "site.add")))) { adviserName =>
        AnswerRow("adviserName.heading", Seq(adviserName), answerIsMessageKey = false,
          None)
      })
    case _ => None
  }

  private def pensionAdviserEmail: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(false) =>
      Option(userAnswers.get(AdviserEmailId).fold[AnswerRow](
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserEmailController.onPageLoad(UpdateMode).url, "site.add")))) { email =>
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(email), answerIsMessageKey = false,
          Some(Link(controllers.register.adviser.routes.AdviserEmailController.onPageLoad(UpdateMode).url)))
      })
    case _ => None
  }

  private def pensionAdviserPhone: Option[AnswerRow] = userAnswers.get(VariationWorkingKnowledgeId) match {

    case Some(false) =>
      Option(userAnswers.get(AdviserPhoneId).fold[AnswerRow](
        AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link(controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(UpdateMode).url, "site.add")))) {
        phone =>
          AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(phone), answerIsMessageKey = false,
            Some(Link(controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(UpdateMode).url)))
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

  def individualContactOnlySections(psaId: String): Seq[SuperSection] = Seq(individualDetailsContactOnlySection(psaId))

  val companySections: Seq[SuperSection] = Seq(companyDetailsSection, directorsSuperSection) ++ pensionAdviserSection.toSeq

  def companyContactOnlySections(psaId: String): Seq[SuperSection] = Seq(companyDetailsContactOnlySection(psaId))

  val partnershipSections: Seq[SuperSection] = Seq(partnershipDetailsSection, partnersSuperSection) ++ pensionAdviserSection.toSeq

  def partnershipContactOnlySections(psaId: String): Seq[SuperSection] = Seq(partnershipDetailsContactOnlySection(psaId))

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
