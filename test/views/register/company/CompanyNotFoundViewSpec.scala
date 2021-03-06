/*
 * Copyright 2021 HM Revenue & Customs
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

import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.company.companyNotFound

class CompanyNotFoundViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyNotFound"
  val view: companyNotFound = app.injector.instanceOf[companyNotFound]


  def createView: () => HtmlFormat.Appendable = () => view()(fakeRequest, messages)

  "CompanyNotFound view" must {
    behave like normalPage(createView, messageKeyPrefix)

    "have link to companies house" in {
      createView must haveLink(
        frontendAppConfig.tellCompaniesHouseCompanyChangesUrl,
        "companies-house-link"
      )
    }

    "have link to HMRC change companies" in {
      createView must haveLink(
        frontendAppConfig.tellHMRCCompanyChangesUrl,
        "hmrc-company-link"
      )
    }

    "have link to tell HMRC about changes" in {
      createView must haveLink(
        frontendAppConfig.tellHMRCChangesUrl,
        "tell-hmrc-link"
      )
    }

    "have link to enter details again" in {
      createView must haveLink(
        controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(NormalMode).url,
        "enter-details-again-link"
      )
    }

  }

}
