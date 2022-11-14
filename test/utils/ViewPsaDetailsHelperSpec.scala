/*
 * Copyright 2022 HM Revenue & Customs
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

import base.{JsonFileReader, SpecBase}
import controllers.register.company.routes._
import controllers.register.partnership.routes._
import identifiers.register.company.directors._
import identifiers.register.partnership.partners._
import models._
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsBoolean, JsObject, Json}
import utils.countryOptions.CountryOptions
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.testhelpers.ViewPsaDetailsBuilder._
import viewmodels._

import java.time.LocalDate

class ViewPsaDetailsHelperSpec extends SpecBase with Matchers {

  import ViewPsaDetailsHelperSpec._

  def validSection(
                    testName: String,
                    headingKey: Option[String],
                    result: Seq[SuperSection],
                    expectedAnswerRows: Seq[AnswerRow]
                  ): Unit = {
    s"display details section with correct labels for $testName" in {
      val actualLabels = actualSeqAnswerRow(result, headingKey).map(_.label).toSet
      val expectedLabels = expectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    s"display details section with correct values for $testName" in {
      val actualValues = actualSeqAnswerRow(result, headingKey).map(_.answer).toSet
      val expectedValues = expectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }

    s"display details section with correct links for $testName" in {
      val actualValues = actualSeqAnswerRow(result, headingKey).map(_.changeUrl).toSet
      val expectedValues = expectedAnswerRows.map(_.changeUrl).toSet

      actualValues mustBe expectedValues
    }
  }

  "ViewPsaDetailsHelper" must {

    behave like validSection(
      testName = "individual details",
      headingKey = None,
      result = individualResult,
      expectedAnswerRows = individualSeqAnswers()
    )

    behave like validSection(
      testName = "individual contact details only",
      headingKey = None,
      result = individualContactOnlyResult,
      expectedAnswerRows = individualContactOnlySeqAnswers()
    )

    behave like validSection(
      testName = "individual details with add link",
      headingKey = None,
      result = individualResultWithAddLink,
      expectedAnswerRows = individualSeqAnswers(noPrevAddr = true)
    )

    behave like validSection(
      testName = "company details",
      headingKey = None,
      result = companyResult,
      expectedAnswerRows = companySeqAnswers()
    )

    behave like validSection(
      testName = "company contact details only",
      headingKey = None,
      result = companyContactOnlyResult,
      expectedAnswerRows = companyContactOnlySeqAnswers()
    )

    behave like validSection(
      testName = "company details with add link",
      headingKey = None,
      result = companyResultWithAddLinks,
      expectedAnswerRows = companySeqAnswers(noPrevAddr = true)
    )

    behave like validSection(
      testName = "partnership details",
      headingKey = None,
      result = partnershipResult,
      expectedAnswerRows = partnershipSeqAnswers()
    )

    behave like validSection(
      testName = "partnership contact details only",
      headingKey = None,
      result = partnershipContactOnlyResult,
      expectedAnswerRows = partnershipContactOnlySeqAnswers()
    )

    behave like validSection(
      testName = "partnership details with add link",
      headingKey = None,
      result = partnershipResultWithAddLinks,
      expectedAnswerRows = partnershipSeqAnswers(noPrevAddr = true)
    )

    "have a super section heading for directors" in {
      companyResult.exists(_.headingKey == directorDetailsSuperSectionKey) mustBe true
    }

    "have add link for directors for only one director" in {
      companyResult.exists(_.addLink.contains(AddLink(
        Link(AddCompanyDirectorsController.onPageLoad(UpdateMode).url, "director-add-link-onlyOne"),
        Some("director-add-link-onlyOne-additionalText"))
      )) mustBe true
    }

    "have add link for directors for less than 10 directors" in {
      companyResultWithTwoDirectors.exists(_.addLink.contains(AddLink(
        Link(AddCompanyDirectorsController.onPageLoad(UpdateMode).url, "director-add-link-lessThanTen"),
        None
      ))) mustBe true
    }

    "have add link for directors for 10 directors" in {
      companyResultWithTenDirectors.exists(_.addLink.contains(AddLink(
        Link(AddCompanyDirectorsController.onPageLoad(UpdateMode).url, "director-add-link-Ten"),
        Some("director-add-link-Ten-additionalText")
      ))) mustBe true
    }

    "have add link for directors for incomplete directors" in {
      companyResultIncomplete.exists(_.addLink.contains(AddLink(
        Link(AddCompanyDirectorsController.onPageLoad(UpdateMode).url, "director-add-link-incomplete"),
        None
      ))) mustBe true
    }

    behave like validSection(
      testName = "director details",
      headingKey = directorDetailsSuperSectionKey,
      result = companyResult,
      expectedAnswerRows = directorsSeqAnswers
    )

    behave like validSection(
      testName = "director details with add links",
      headingKey = directorDetailsSuperSectionKey,
      result = companyResultWithAddLinks,
      expectedAnswerRows = directorsSeqAnswersWithAddLinks
    )

    "have a super section heading for partners" in {
      partnershipResult.exists(_.headingKey == partnerDetailsSuperSectionKey) mustBe true
    }

    "have add link for partners for only one partner" in {
      partnershipResult.exists(_.addLink.contains(AddLink(
        Link(AddPartnerController.onPageLoad(UpdateMode).url, "partner-add-link-lessThanTwo"),
        Some("partner-add-link-onlyOne-additionalText"))
      )) mustBe true
    }

    "have add link for partners for only two partner" in {
      partnershipResultWithTwoPartners.exists(_.addLink.contains(AddLink(
        Link(AddPartnerController.onPageLoad(UpdateMode).url, "partner-add-link-lessThanTwo"),
        Some("partner-add-link-onlyTwo-additionalText"))
      )) mustBe true
    }

    "have add link for partners for less than 10 partners" in {
      partnershipResultWithThreePartners.exists(_.addLink.contains(AddLink(
        Link(AddPartnerController.onPageLoad(UpdateMode).url, "partner-add-link-lessThanTen"),
        None)
      )) mustBe true
    }

    "have add link for partners for 10 partners" in {
      partnershipResultWithTenPartners.exists(_.addLink.contains(AddLink(
        Link(AddPartnerController.onPageLoad(UpdateMode).url, "partner-add-link-Ten"),
        Some("partner-add-link-Ten-additionalText"))
      )) mustBe true
    }

    "have add link for partners if any of the partners is incomplete" in {
      partnershipResultIncomplete.exists(_.addLink.contains(AddLink(
        Link(AddPartnerController.onPageLoad(UpdateMode).url, "partner-add-link-incomplete"),
        None
      ))) mustBe true
    }

    behave like validSection(
      testName = "partner details",
      headingKey = partnerDetailsSuperSectionKey,
      result = partnershipResult,
      expectedAnswerRows = partnersSeqAnswers
    )

    behave like validSection(
      testName = "partner details with add links",
      headingKey = partnerDetailsSuperSectionKey,
      result = partnershipResultWithAddLinks,
      expectedAnswerRows = partnersSeqAnswersWithAddLinks
    )

    "have a super section heading for pension adviser" in {
      partnershipResult.exists(_.headingKey == pensionAdvisorSuperSectionKey) mustBe true
    }

    behave like validSection(
      testName = "pension advisor details",
      headingKey = pensionAdvisorSuperSectionKey,
      result = partnershipResult,
      expectedAnswerRows = pensionAdviserSeqAnswers
    )

  }
}

object ViewPsaDetailsHelperSpec extends SpecBase with JsonFileReader {
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private def psaDetailsHelper(userAnswers: UserAnswers) = new ViewPsaDetailsHelper(userAnswers, countryOptions)

  private val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")

  private val individualUserAnswersWithoutPrevAddr =
    readJsonFromFile("/data/psaIndividualUserAnswers.json").as[JsObject] - "individualPreviousAddress"

  private val companyUserAnswers =
    readJsonFromFile("/data/psaCompanyUserAnswers.json")

  private val companyUserAnswersIncomplete = readJsonFromFile("/data/psaCompanyUserAnswers.json").as[JsObject] -
    "directors" + ("directors" -> Json.arr(
    Json.obj(DirectorNameId.toString -> PersonName("John", "One"))))

  private val companyUserAnswersWithTwoDirectors = UserAnswers(readJsonFromFile("/data/psaCompanyUserAnswers.json").as[JsObject] -
    "directors").completeDirector(index = 0).completeDirector(index = 1)

  private val companyUserAnswersWithTenDirectors = UserAnswers(readJsonFromFile("/data/psaCompanyUserAnswers.json").as[JsObject] -
    "directors").completeDirector(0).completeDirector(index = 1).completeDirector(index = 2).completeDirector(index = 3).completeDirector(index = 4).
    completeDirector(index = 5).completeDirector(index = 6).completeDirector(index = 7).completeDirector(index = 8).completeDirector(index = 9)

  private val companyUserAnswersWithAddLinks = readJsonFromFile("/data/psaCompanyUserAnswers.json").as[JsObject] -
    "companyPreviousAddress" + ("companyConfirmPreviousAddress" -> JsBoolean(false)) - "directors" + ("directors" -> Json.arr(
    Json.obj(
      DirectorNameId.toString -> PersonName("test first name", "test last name"),
      DirectorDOBId.toString -> LocalDate.of(2019, 10, 23),
      DirectorNoNINOReasonId.toString -> "reason",
      DirectorAddressYearsId.toString -> AddressYears.UnderAYear.toString,
      DirectorConfirmPreviousAddressId.toString -> false,
      DirectorNoUTRReasonId.toString -> "reason"
    )
  ))
  private val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")

  private val partnershipUserAnswersIncomplete = readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] - "partners" +
    ("partners" -> Json.arr(
      Json.obj(PartnerNameId.toString -> PersonName("John", "One"))
    ))

  private val partnershipUserAnswersWithTwoPartners = UserAnswers(readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] -
    "partners").completePartner(index = 0).completePartner(index = 1)

  private val partnershipUserAnswersWithThreePartners = UserAnswers(readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] -
    "partners").completePartner(index = 0).completePartner(index = 1).completePartner(index = 2)

  private val partnershipUserAnswersWithTenPartners = UserAnswers(readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] -
    "partners").completePartner(index = 0).completePartner(index = 1).completePartner(index = 2).completePartner(index = 3).completePartner(index = 4).
    completePartner(index = 5).completePartner(index = 6).completePartner(index = 7).completePartner(index = 8).completePartner(index = 9)

  private val partnershipUserAnswersWithAddLinks = readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] -
    "partnershipPreviousAddress" + ("partnershipConfirmPreviousAddress" -> JsBoolean(false)) -
    "partners" + ("partners" -> Json.arr(
    Json.obj(
      PartnerNameId.toString -> PersonName("test first name", "test last name"),
      PartnerDOBId.toString -> LocalDate.now(),
      PartnerNoNINOReasonId.toString -> "reason",
      PartnerAddressYearsId.toString -> AddressYears.UnderAYear.toString,
      PartnerConfirmPreviousAddressId.toString -> false,
      PartnerNoUTRReasonId.toString -> "reason"
    )
  ))

  val address: Address = Address("Telford1", "Telford2", Some("Telford3"), Some("Telford4"), Some("TF3 4ER"), "GB")
  val psaAddress: Address = Address("addline1", "addline2", Some("addline3"), Some("addline4"), Some("56765"), "AD")
  val previousAddress: Address = Address("London1", "London2", Some("London3"), Some("London4"), Some("LN12 4DC"), "GB")

  private val individualResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(individualUserAnswers)).individualSections

  private def individualContactOnlyResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(individualUserAnswers)).individualContactOnlySections("A2100005")

  private val individualResultWithAddLink: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(individualUserAnswersWithoutPrevAddr)).individualSections
  private val companyResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(companyUserAnswers)).companySections
  private val companyContactOnlyResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(companyUserAnswers)).companyContactOnlySections("A2100005")
  private val companyResultIncomplete: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(companyUserAnswersIncomplete)).companySections
  private val companyResultWithTwoDirectors: Seq[SuperSection] =
    psaDetailsHelper(companyUserAnswersWithTwoDirectors).companySections
  private val companyResultWithTenDirectors: Seq[SuperSection] =
    psaDetailsHelper(companyUserAnswersWithTenDirectors).companySections
  private val companyResultWithAddLinks: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(companyUserAnswersWithAddLinks)).companySections
  private val partnershipResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(partnershipUserAnswers)).partnershipSections
  private val partnershipContactOnlyResult: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(partnershipUserAnswers)).partnershipContactOnlySections("A2100005")
  private val partnershipResultIncomplete: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(partnershipUserAnswersIncomplete)).partnershipSections
  private val partnershipResultWithTwoPartners: Seq[SuperSection] =
    psaDetailsHelper(partnershipUserAnswersWithTwoPartners).partnershipSections
  private val partnershipResultWithThreePartners: Seq[SuperSection] =
    psaDetailsHelper(partnershipUserAnswersWithThreePartners).partnershipSections
  private val partnershipResultWithTenPartners: Seq[SuperSection] =
    psaDetailsHelper(partnershipUserAnswersWithTenPartners).partnershipSections
  private val partnershipResultWithAddLinks: Seq[SuperSection] =
    psaDetailsHelper(UserAnswers(partnershipUserAnswersWithAddLinks)).partnershipSections

  private val partnerDetailsSuperSectionKey = Some("partner.supersection.header")
  private val directorDetailsSuperSectionKey = Some("director.supersection.header")
  private val pensionAdvisorSuperSectionKey = Some("pensionAdvisor.section.header")

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)
}
