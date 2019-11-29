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

import forms.register.AreYouInUKFormProvider
import models.{CheckMode, Mode, NormalMode}
import play.api.data.Form
import play.twirl.api.Html
import viewmodels.{AreYouInUKViewModel, Message}
import views.behaviours.{ViewBehaviours, YesNoViewBehaviours}
import views.html.register.areYouInUK

class IndividualAreYouInUKViewSpec extends ViewBehaviours with YesNoViewBehaviours {

  private val messageKeyPrefix = "areYouInUKIndividual"

  private def viewmodel(mode: Mode) =
    AreYouInUKViewModel(mode,
      postCall = controllers.register.individual.routes.IndividualAreYouInUKController.onSubmit(mode),
      title = Message("areYouInUKIndividual.title"),
      heading = Message("areYouInUKIndividual.heading"),
      p1 = Some("areYouInUKIndividual.check.selectedUkAddress"),
      p2 = Some("areYouInUKIndividual.check.provideNonUkAddress"),
      secondaryLabel = None
    )


  val formProvider = new AreYouInUKFormProvider

  val form: Form[Boolean] = formProvider()

  val view: areYouInUK = app.injector.instanceOf[areYouInUK]

  private def createView(mode: Mode = NormalMode): () => Html = () =>
    view(
      form,
      viewmodel(mode)
    )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) =>
    view(
      form,
      viewmodel(NormalMode)
    )(fakeRequest, messages)

  "Are you in UK view" must {
    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithSubmitButton(createView())

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = viewmodel(NormalMode).postCall.url,
      messageKey = s"$messageKeyPrefix.heading"
    )

    "not display dynamic content of CheckMode in NormalMode" in {
      createView() mustNot haveDynamicText("areYouInUKIndividual.check.selectedUkAddress")
      createView() mustNot haveDynamicText("areYouInUKIndividual.check.provideNonUkAddress")
    }

    "display dynamic content of CheckMode in NormalMode" in {
      createView(CheckMode) must haveDynamicText("areYouInUKIndividual.check.selectedUkAddress")
      createView(CheckMode) must haveDynamicText("areYouInUKIndividual.check.provideNonUkAddress")
    }
  }

}
