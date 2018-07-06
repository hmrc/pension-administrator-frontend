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

package views.register.company

import play.api.data.Form
import controllers.register.company.routes
import forms.register.company.MoreThanTenDirectorsFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.company.moreThanTenDirectors

class MoreThanTenDirectorsViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "moreThanTenDirectors"

  val form = new MoreThanTenDirectorsFormProvider()()

  def createView = () => moreThanTenDirectors(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => moreThanTenDirectors(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

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
