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

package views.register.company

import controllers.register.company.routes
import models.CheckMode
import views.behaviours.ViewBehaviours
import views.html.register.company.companyReview

class CompanyReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyReview"
  val companyName = "test company name"
  val directors = Seq("director a", "director b", "director c")
  val tenDirectors = Seq("director a", "director b", "director c", "director d", "director e",
    "director f", "director g", "director h", "director i", "director j")

  def createView = () => companyReview(frontendAppConfig, companyName, directors)(fakeRequest, messages)
  def createSecView = () => companyReview(frontendAppConfig, companyName, tenDirectors)(fakeRequest, messages)

  "CompanyReview view" must {
    behave like normalPage(createView, messageKeyPrefix)
    behave like pageWithSecondaryHeader(createView, messages("common.individual.secondary.heading"))
    behave like pageWithBackLink(createView)
  }

  "display company name" in {
    createView must haveDynamicText(companyName)
  }

  "have link to edit company details" in {
    createView must haveLink(
      routes.CompanyDetailsController.onPageLoad(CheckMode).url, "edit-company-details"
    )
  }

  "have link to edit director details when there are less than 10 directors" in {
    createView must haveLink(
      routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "edit-director-details"
    )
    createView must haveDynamicText("companyReview.directors.editLink")
  }

  "have link to edit directors when there are 10 directors" in {
    createView must haveLink(
      routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url, "edit-director-details"
    )
    createSecView must haveDynamicText("companyReview.directors.changeLink")
  }

  "contain list of directors" in {
    for(director <- directors)
      createView must haveDynamicText(director)
  }

}
