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

import base.SpecBase
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.pensionSchemePractitioner

class PensionSchemePractitionerViewSpec extends ViewBehaviours {

  import PensionSchemePractitionerViewSpec._

  "pensionSchemePractitioner" must {

    behave like normalPage(createView(this), messageKeyPrefix)

    "display the lede text" in {
      createView(this) must haveDynamicText("pensionSchemePractitioner.lede")
    }

    "display the explanation text" in {
      createView(this) must haveDynamicText("pensionSchemePractitioner.explanation")
    }

    "display the continue link" in {
      createView(this) must haveLink(frontendAppConfig.tpssUrl, "continueTpssLink")
    }

  }

}

object PensionSchemePractitionerViewSpec {

  val messageKeyPrefix: String = "pensionSchemePractitioner"

  def createView(base: SpecBase): () => HtmlFormat.Appendable =
    () => pensionSchemePractitioner(base.frontendAppConfig)(base.fakeRequest, base.messages)

}
