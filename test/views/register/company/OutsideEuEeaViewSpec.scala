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

import views.behaviours.ViewBehaviours
import views.html.register.company.outsideEuEea

class OutsideEuEeaViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "outsideEuEea"
  val organisationName = "Test company name"
  val country = "Canada"

  def createView = () => outsideEuEea(frontendAppConfig, organisationName, country)(fakeRequest, messages)

  "OutsideEuEea view" must {
    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix, "body")

    "display the correct page heading" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", organisationName)
    }

    "display dynamic text about current country" in {
      createView must haveDynamicText(messages("outsideEuEea.currentCountry.text", organisationName, country))
    }
  }
}
