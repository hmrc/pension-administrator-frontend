/*
 * Copyright 2018 HM Revenue & Customs
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
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.MoreThanTenViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.moreThanTen

class MoreThanTenViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "moreThanTenDirectors"

  val form = new MoreThanTenFormProvider()()

  def viewModel =
    MoreThanTenViewModel(
      title = "moreThanTenDirectors.title",
      heading = "moreThanTenDirectors.heading",
      hint = "moreThanTenDirectors.hint",
      postCall = controllers.register.company.routes.MoreThanTenDirectorsController.onSubmit(NormalMode),
      id = new TypedIdentifier[Boolean] {}
    )

  def createView: () => HtmlFormat.Appendable = () => moreThanTen(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => moreThanTen(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  "MoreThanTenDirectors view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like yesNoPage(createViewUsingForm,
      messageKeyPrefix,
      routes.MoreThanTenDirectorsController.onSubmit(NormalMode).url,
      s"$messageKeyPrefix.heading",
      Some(s"$messageKeyPrefix.hint"))
  }
}
