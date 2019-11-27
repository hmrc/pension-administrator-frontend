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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.individual.youWillNeedToUpdate

class YouWillNeedToUpdateViewSpec extends ViewBehaviours {


  val tellHMRCChangesUrl = "https://www.gov.uk/tell-hmrc-change-of-details"
  val messageKeyPrefix = "youWillNeedToUpdate"

  val view: youWillNeedToUpdate = app.injector.instanceOf[youWillNeedToUpdate]

  def createView: () => HtmlFormat.Appendable = () =>
    view()(fakeRequest, messages)

  "YouWillNeedToUpdate view" must {
    behave like normalPage(createView, messageKeyPrefix)

    "have link for enter address manually" in {
      createView must haveLink(
        tellHMRCChangesUrl,
        "inform-hmrc-link"
      )
    }
  }

}
