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

package utils

import models.PsaSubscription.{CorrespondenceAddress, DirectorOrPartner, PsaSubscription}
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection, Message, SuperSection}

class PsaDetailsHelper(psaDetails: PsaSubscription, countryOptions: CountryOptions)(implicit messages: Messages) {

  import PsaDetailsHelper._

  private val individualDetailsSection = SuperSection(
    None,
    Seq(AnswerSection(
      None,
      Seq(
        individualDateOfBirth,
        individualNino,
        psaAddress,
        previousAddressExists(psaDetails.individual map(_.fullName)),
        psaPreviousAddress,
        emailAddress,
        phoneNumber
      ).flatten
    )
   )
  )

  private val organisationDetailsSection = SuperSection(
    None,
    Seq(
        AnswerSection(
          None,
          Seq(
            vatNumber,
            payeNumber,
            crn,
            psaAddress,
            previousAddressExists(psaDetails.organisationOrPartner map(_.name)),
            psaPreviousAddress,
            emailAddress,
            phoneNumber
          ).flatten
        )
    )
  )

  private def directorOrPartnerSection(person: DirectorOrPartner) = AnswerSection(
    Some(person.fullName),
    Seq(directorOrPartnerDob(person),
      directorOrPartnerNino(person),
      directorOrPartnerUtr(person),
      directorOrPartnerAddress(person),
      directorOrPartnerPrevAddress(person),
      directorOrPartnerEmail(person),
      directorOrPartnerPhone(person)
    ).flatten
  )

  private val directorsOrPartnersSection = psaDetails.directorsOrPartners map { list =>
    for(person <- list) yield directorOrPartnerSection(person)
  }

  private val pensionAdvisorSection = SuperSection(
    Some(messages("pensionAdvisor.section.header")),
    Seq(
      AnswerSection(
        None,
        Seq(
          pensionAdvisor,
          pensionAdvisorEmail,
          pensionAdvisorAddress
        ).flatten
    )
   )
  )

  //Director Or Partner
  private def directorOrPartnerDob(person: DirectorOrPartner): Option[AnswerRow] =
    Some(AnswerRow("cya.label.dob", Seq(person.dateOfBirth.toString), false, None))

  private def directorOrPartnerNino(person: DirectorOrPartner): Option[AnswerRow] =
    person.nino map { nino =>
          AnswerRow("common.nino", Seq(nino), false, None)
    }

  private def directorOrPartnerUtr(person: DirectorOrPartner): Option[AnswerRow] =
    person.utr map { utr =>
      AnswerRow("common.nino", Seq(utr), false, None)
    }

  private def directorOrPartnerAddress(person: DirectorOrPartner): Option[AnswerRow] =
    person.correspondenceDetails map { details =>
    AnswerRow("cya.label.address", addressAnswer(details.address, countryOptions), false, None)
  }

  private def directorOrPartnerPrevAddress(person: DirectorOrPartner): Option[AnswerRow] =
    person.previousAddress map { address =>
      AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), false, None)
    }

  private def directorOrPartnerPhone(person: DirectorOrPartner): Option[AnswerRow] =
    person.correspondenceDetails flatMap(_.contactDetails map { details =>
      AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(details.telephone), false, None)
    })

  private def directorOrPartnerEmail(person: DirectorOrPartner): Option[AnswerRow] =
    person.correspondenceDetails flatMap(_.contactDetails flatMap(_.email map { email =>
      AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(email), false, None)
    }))

  //Individual PSA
  private def individualDateOfBirth: Option[AnswerRow] = psaDetails.individual map { ind =>
    AnswerRow("cya.label.dob", Seq(DateHelper.formatDateWithSlash(ind.dateOfBirth)), false, None)
  }

  private def individualNino: Option[AnswerRow] = psaDetails.customerIdentification.typeOfId flatMap { id =>
    if (id.equalsIgnoreCase("NINO")) {
      psaDetails.customerIdentification.number map { nino =>
        AnswerRow("common.nino", Seq(nino), false, None)
      }
    } else {
      None
    }
  }

  //Company or Partnership PSA
  private def vatNumber: Option[AnswerRow] = psaDetails.organisationOrPartner flatMap(_.vatRegistration.map { vat =>
    AnswerRow("vat.label", Seq(vat), false, None)
  })

  private def payeNumber: Option[AnswerRow] = psaDetails.organisationOrPartner flatMap(_.paye.map { paye =>
    AnswerRow("paye.label", Seq(paye), false, None)
  })

  private def crn: Option[AnswerRow] = psaDetails.organisationOrPartner flatMap(_.crn.map { crn =>
    AnswerRow("crn.label", Seq(crn), false, None)
  })

  //common to all PSAs
  private def psaAddress: Option[AnswerRow] =
    Some(AnswerRow("cya.label.address", addressAnswer(psaDetails.address, countryOptions), false, None))

  private def previousAddressExists(name: Option[String]): Option[AnswerRow] = Some(AnswerRow(
    Message("moreThan12Months.label", name.getOrElse("")).resolve,
    Seq(messages(s"sameAddress.label.${psaDetails.isSameAddressForLast12Months}")), false, None
  ))

  private def psaPreviousAddress: Option[AnswerRow] = psaDetails.previousAddress map { address =>
    AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(address, countryOptions), false, None)
  }

  private def phoneNumber: Option[AnswerRow] =
    Some(AnswerRow("phone.label", Seq(psaDetails.contact.telephone), false, None))

  private def emailAddress: Option[AnswerRow] = psaDetails.contact.email map { emailAddress =>
    AnswerRow("email.label", Seq(emailAddress), false, None)
  }

  //Pension Advisor
  private def pensionAdvisor: Option[AnswerRow] = psaDetails.pensionAdvisor map { advisor =>
    AnswerRow("pensions.advisor.label", Seq(advisor.name), false, None)
  }

  private def pensionAdvisorEmail: Option[AnswerRow] = psaDetails.pensionAdvisor flatMap (
    _.contactDetails.flatMap(_.email.map { email =>
      AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(email), false, None)
    }
    ))

  private def pensionAdvisorAddress: Option[AnswerRow] = psaDetails.pensionAdvisor map { advisor =>
    AnswerRow("pensions.advisor.label", Seq(advisor.name), false, None)
  }

  val directorsOrPartnersSuperSection = SuperSection(Some("Director details"), directorsOrPartnersSection.getOrElse(Seq.empty))

  val individualSections = Seq(individualDetailsSection, pensionAdvisorSection)
  val organisationSections = Seq(organisationDetailsSection, directorsOrPartnersSuperSection, pensionAdvisorSection)

}

object PsaDetailsHelper {
  def addressAnswer(address: CorrespondenceAddress, countryOptions: CountryOptions): Seq[String] = {
    val country = countryOptions.options
      .find(_.value == address.countryCode)
      .map(_.label)
      .getOrElse(address.countryCode)

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postalCode.map(postcode => s"$postcode,"),
      Some(country)
    ).flatten
  }
}

