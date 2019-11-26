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

import forms.register.VariationWorkingKnowledgeFormProvider
import models.{CheckUpdateMode, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.variationWorkingKnowledge

class VariationWorkingKnowledgeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "variationWorkingKnowledge"

  val form = new VariationWorkingKnowledgeFormProvider()()

  val view: variationWorkingKnowledge = app.injector.instanceOf[variationWorkingKnowledge]

  def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    view(form, Some("Mark Wright"), mode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, Some("Mark Wright"), UpdateMode)(fakeRequest, messages)

  "variationWorkingKnowledge view" must {
    behave like normalPage(createView(), messageKeyPrefix, "p1", "p2", "p3")

    behave like yesNoPage(createViewUsingForm,
      messageKeyPrefix,
      controllers.register.routes.VariationWorkingKnowledgeController.onSubmit(UpdateMode).url,
      s"$messageKeyPrefix.heading"
    )
  }

  "variationWorkingKnowledge view" when {
    Seq(UpdateMode, CheckUpdateMode).foreach { mode =>
      s"in $mode mode" must {
        behave like pageWithReturnLink(createView(mode), controllers.routes.PsaDetailsController.onPageLoad().url)
      }
    }
  }
  app.stop()
}
