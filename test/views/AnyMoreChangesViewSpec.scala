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

import controllers.vary.routes
import forms.AnyMoreChangesFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.vary.anyMoreChanges

class AnyMoreChangesViewSpec extends YesNoViewBehaviours {
  val schemeName = Some("Scheme x")
  val messageKeyPrefix = "any_more_changes"

  val form = new AnyMoreChangesFormProvider()()

  def createView: () => HtmlFormat.Appendable = () =>
    anyMoreChanges(frontendAppConfig, form, schemeName)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    anyMoreChanges(appConfig(isHubEnabled = true), form, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    anyMoreChanges(frontendAppConfig, form, schemeName)(fakeRequest, messages)

  "Any More Changes view" must {

    behave like normalPage(createView, messageKeyPrefix,
      expectedGuidanceKeys = "p1", "p2")

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.AnyMoreChangesController.onSubmit().url,
      messageKey = s"$messageKeyPrefix.title"
    )

    behave like pageWithSubmitButton(createView)

    //behave like pageWithReturnLink(createView, getReturnLink)

  }
}
