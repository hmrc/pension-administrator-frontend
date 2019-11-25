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

package views.register.individual

import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.register.individual.outsideEuEea

class OutsideEuEeaViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "outsideEuEea.individual"
  val country = "Canada"

  val view: outsideEuEea = app.injector.instanceOf[outsideEuEea]

  def createView: () => Html = () =>
    view(country)(fakeRequest, messages)

  "OutsideEuEea view" must {
    behave like normalPageWithPageTitleCheck(createView, messageKeyPrefix, "body")

    "display the correct page heading" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
    }

    "display dynamic text about current country" in {
      createView must haveDynamicText(messages("outsideEuEea.individual.currentCountry.text", country))
    }

    "display link to return to gov uk" in {
      createView must haveLink(frontendAppConfig.govUkUrl, "return-gov-uk")
    }
  }
}
