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

package views.register.partnership

import controllers.register.partnership.routes
import models.NormalMode
import views.behaviours.ViewBehaviours
import views.html.register.partnership.partnershipReview

class PartnershipReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipReview"
  val partnershipName = "test partnership name"
  val partners = Seq("partner a", "partner b", "partner c")
  val tenPartners = Seq("partner a", "partner b", "partner c", "partner d", "partner e",
    "partner f", "partner g", "partner h", "partner i", "partner j")

  def createView = () => partnershipReview(frontendAppConfig, partnershipName, partners)(fakeRequest, messages)

  def createSecView = () => partnershipReview(frontendAppConfig, partnershipName, tenPartners)(fakeRequest, messages)

  "PartnershipReview view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "display partnership name" in {
    createView must haveDynamicText(partnershipName)
  }

  "have link to edit partnership details" in {
    createView must haveLink(
      routes.CheckYourAnswersController.onPageLoad.url, "edit-partnership-details"
    )
  }

  "have link to edit partner details when there are less than 10 partners" in {
    createView must haveLink(
      routes.AddPartnerController.onPageLoad(NormalMode).url, "edit-partner-details"
    )
    createView must haveDynamicText("partnershipReview.partners.editLink")
  }

  "have link to edit partners when there are 10 partners" in {
    createView must haveLink(
      routes.AddPartnerController.onPageLoad(NormalMode).url, "edit-partner-details"
    )
    createSecView must haveDynamicText("partnershipReview.partners.changeLink")
  }

  "contain list of partners" in {
    for (partner <- partners)
      createView must haveDynamicText(partner)
  }

}
