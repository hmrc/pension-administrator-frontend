/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.register.company.routes
import forms.MoreThanTenFormProvider
import identifiers.TypedIdentifier
import models.{Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.{Message, MoreThanTenViewModel}
import views.behaviours.YesNoViewBehaviours
import views.html.moreThanTen

class MoreThanTenViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "moreThanTenDirectors"

  val form = new MoreThanTenFormProvider()("moreThanTenDirectors.error.required")

  def viewModel: MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenDirectors.title",
      heading = Message("moreThanTenDirectors.heading"),
      hint = "moreThanTenDirectors.hint",
      postCall = controllers.register.company.routes.MoreThanTenDirectorsController.onSubmit(NormalMode),
      id = new TypedIdentifier[Boolean] {},
      psaName = Some("test psa"),
      errorKey = "moreThanTenDirectors.error.required"
    )

  val view: moreThanTen = app.injector.instanceOf[moreThanTen]

  def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () => view(form, viewModel, mode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => view(form, viewModel, NormalMode)(fakeRequest, messages)

  "MoreThanTenDirectors view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.MoreThanTenDirectorsController.onSubmit(NormalMode).url,
      messageKey = s"$messageKeyPrefix.heading"
    )

    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }

}
