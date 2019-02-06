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

import java.time.LocalDate

import base.{JsonFileReader, SpecBase}
import identifiers.register.company.directors.{DirectorDetailsId, DirectorNinoId, DirectorUniqueTaxReferenceId}
import identifiers.register.partnership.partners.{PartnerDetailsId, PartnerNinoId, PartnerUniqueTaxReferenceId}
import models._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, Json}
import utils.countryOptions.CountryOptions
import utils.testhelpers.ViewPsaDetailsBuilder._
import viewmodels.{AddLink, AnswerRow, Link, SuperSection}

class ViewPsaDetailsHelperSpec extends WordSpec with MustMatchers {

  import ViewPsaDetailsHelperSpec._

  def validSection(testName: String, headingKey: Option[String], result: Seq[SuperSection], expectedAnswerRows: Seq[AnswerRow]): Unit = {
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

    behave like validSection(testName = "individual details", headingKey = None, result = individualResult, expectedAnswerRows = individualSeqAnswers)

    behave like validSection(testName = "company details", headingKey = None, result = companyResult, expectedAnswerRows = companySeqAnswers)

    behave like validSection(testName = "partnership details", headingKey = None,
      result = partnershipResult, expectedAnswerRows = partnershipSeqAnswers)

    "have a super section heading for directors" in {
      companyResult.exists(_.headingKey == directorDetailsSuperSectionKey) mustBe true
    }

    "have add link for directors for only one director" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "director-add-link-onlyOne"),
        None
      ))
    }

    "have add link for directors for less than 10 directors" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "director-add-link-lessThanTen"),
        None
      ))
    }

    "have add link for directors for 10 directors" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "director-add-link-Ten"),
        Some("director-add-link-Ten-additionalText")
      ))
    }

    behave like validSection(testName = "director details", headingKey = directorDetailsSuperSectionKey,
      result = companyResult, expectedAnswerRows = directorsSeqAnswers)

    behave like validSection(testName = "director details with add links", headingKey = directorDetailsSuperSectionKey,
      result = companyResultWithAddLinks, expectedAnswerRows = directorsSeqAnswersWithAddLinks)

    "have a super section heading for partners" in {
      partnershipResult.exists(_.headingKey == partnerDetailsSuperSectionKey) mustBe true
    }

    "have add link for partners for only one director" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "partner-add-link-onlyOne"),
        None
      ))
    }

    "have add link for partners for less than 10 directors" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "partner-add-link-lessThanTen"),
        None
      ))
    }

    "have add link for partners for 10 directors" in {
      companyResult.exists(_.addLink == AddLink(
        Link(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "partner-add-link-Ten"),
        Some("partner-add-link-Ten-additionalText")
      ))
    }

    behave like validSection(testName = "partner details", headingKey = partnerDetailsSuperSectionKey,
      result = partnershipResult, expectedAnswerRows = partnersSeqAnswers)

    behave like validSection(testName = "partner details with add links", headingKey = partnerDetailsSuperSectionKey,
      result = partnershipResultWithAddLinks, expectedAnswerRows = partnersSeqAnswersWithAddLinks)

    "have a super section heading for pension adviser" in {
      partnershipResult.exists(_.headingKey == pensionAdvisorSuperSectionKey) mustBe true
    }

    behave like validSection(testName = "pension advisor details", headingKey = pensionAdvisorSuperSectionKey,
      result = partnershipResult, expectedAnswerRows = pensionAdviserSeqAnswers)
  }
}

object ViewPsaDetailsHelperSpec extends SpecBase with JsonFileReader {
  private val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  private val companyUserAnswers = readJsonFromFile("/data/psaCompanyUserAnswers.json")

  private val companyUserAnswersWithAddLinks = readJsonFromFile("/data/psaCompanyUserAnswers.json").as[JsObject] - "directors" + ("directors" -> Json.arr(
    Json.obj(
      DirectorDetailsId.toString -> PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now()),
      DirectorNinoId.toString -> Nino.No("reason"),
      DirectorUniqueTaxReferenceId.toString -> UniqueTaxReference.No("reason")
    )
  ))
  private val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")
  private val partnershipUserAnswersWithAddLinks = readJsonFromFile("/data/psaPartnershipUserAnswers.json").as[JsObject] - "partners" + (
    "partners" -> Json.arr(
      Json.obj(
        PartnerDetailsId.toString -> PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now()),
        PartnerNinoId.toString -> Nino.No("reason"),
        PartnerUniqueTaxReferenceId.toString -> UniqueTaxReference.No("reason")
      )
    ))

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private def psaDetailsHelper(userAnswers: UserAnswers) = new ViewPsaDetailsHelper(userAnswers, countryOptions)
  val address = Address("Telford1", "Telford2",Some("Telford3"), Some("Telford4"), Some("TF3 4ER"), "GB")
  val psaAddress = Address("addline1", "addline2", Some("addline3"), Some("addline4"), Some("56765"), "AD")
  val previousAddress = Address("London1", "London2", Some("London3"), Some("London4"), Some("LN12 4DC"), "GB")

  private val individualResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(individualUserAnswers)).individualSections
  private val companyResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(companyUserAnswers)).companySections
  private val companyResultWithAddLinks: Seq[SuperSection] = psaDetailsHelper(UserAnswers(companyUserAnswersWithAddLinks)).companySections
  private val partnershipResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(partnershipUserAnswers)).partnershipSections
  private val partnershipResultWithAddLinks: Seq[SuperSection] = psaDetailsHelper(UserAnswers(partnershipUserAnswersWithAddLinks)).partnershipSections

  private val partnerDetailsSuperSectionKey = Some("partner.supersection.header")
  private val directorDetailsSuperSectionKey = Some("director.supersection.header")
  private val pensionAdvisorSuperSectionKey = Some("pensionAdvisor.section.header")

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)
}
