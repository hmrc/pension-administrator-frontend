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

package views.vary

import controllers.vary.routes
import forms.vary.AnyMoreChangesFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.DateHelper
import views.behaviours.YesNoViewBehaviours
import views.html.vary.anyMoreChanges

class AnyMoreChangesViewSpec extends YesNoViewBehaviours {
  val messageKeyPrefix = "anyMoreChanges"

  val form = new AnyMoreChangesFormProvider()()

  def createView: () => HtmlFormat.Appendable = () =>
    anyMoreChanges(frontendAppConfig, form)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    anyMoreChanges(appConfig(isHubEnabled = true), form)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    anyMoreChanges(frontendAppConfig, form)(fakeRequest, messages)

  "Any More Changes view" must {

    behave like normalPage(createView, messageKeyPrefix,
      expectedGuidanceKeys = "p1", "p2")

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.AnyMoreChangesController.onSubmit().url,
      messageKey = s"$messageKeyPrefix.title"
    )

    "display the paragraph with date(current date plus 28 days)" in {
      createView must haveDynamicText("anyMoreChanges.p3",
        DateHelper.dateTimeAfterGivenDays(frontendAppConfig.daysDataSaved))
    }

    behave like pageWithSubmitButton(createView)
  }
}
