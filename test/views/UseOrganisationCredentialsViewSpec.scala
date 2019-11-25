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

package views

import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.useOrganisationCredentials

class UseOrganisationCredentialsViewSpec extends ViewBehaviours {

  import UseOrganisationCredentialsViewSpec._

  "UseOrganisationCredentials" must {

    behave like normalPage(createView(), messageKeyPrefix)

    "display the p1 text" in {
      createView() must haveDynamicText("useOrganisationCredentials.p1")
    }

    "display the gg link from p2" in {
      val doc = Jsoup.parse(createView().apply().toString())
      doc must haveLinkWithUrlAndContent("p2-login-link", frontendAppConfig.loginUrl,
        Message("useOrganisationCredentials.p2.link").resolve)
    }

    "display the gg link from p3" in {
      val doc = Jsoup.parse(createView().apply().toString())
      doc must haveLinkWithUrlAndContent("p3-login-link", frontendAppConfig.loginUrl,
        Message("useOrganisationCredentials.p3.link").resolve)
    }
  }
}

object UseOrganisationCredentialsViewSpec extends ViewSpecBase {

  val messageKeyPrefix: String = "useOrganisationCredentials"

  val view: useOrganisationCredentials = app.injector.instanceOf[useOrganisationCredentials]

  def createView(): () => HtmlFormat.Appendable = () =>
    view()(fakeRequest, messages)

}


