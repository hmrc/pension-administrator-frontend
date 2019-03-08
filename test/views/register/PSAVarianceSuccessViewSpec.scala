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

package views.register

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.psaVarianceSuccess

class PSAVarianceSuccessViewSpec extends ViewBehaviours {
  private val messageKeyPrefix = "psaVarianceSuccess"

  private val returnLink = controllers.routes.PsaDetailsController.onPageLoad().url

  private val createView: () => HtmlFormat.Appendable = () =>
    psaVarianceSuccess(frontendAppConfig, Some("Mark Wright"))(fakeRequest, messages)


  "noLongerFitAndProper view" must {

    behave like normalPage(createView, messageKeyPrefix)

    "have a link to 'print this screen'" in {
      createView must haveLinkOnClick("window.print();return false;", "print-this-page-link")
    }

    behave like pageWithReturnLink(createView, returnLink)
  }
}
