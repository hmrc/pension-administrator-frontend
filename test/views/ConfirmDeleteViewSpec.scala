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

import controllers.register.partnership.partners.routes
import forms.ConfirmDeleteFormProvider
import models.{Index, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.ConfirmDeleteViewModel
import views.behaviours.ViewBehaviours
import views.html.confirmDelete

class ConfirmDeleteViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmDelete"

  val firstIndex = Index(0)

  val view: confirmDelete = app.injector.instanceOf[confirmDelete]

  val viewModel = ConfirmDeleteViewModel(
    routes.ConfirmDeletePartnerController.onSubmit(firstIndex, NormalMode),
    routes.ConfirmDeletePartnerController.onPageLoad(firstIndex, NormalMode),
    s"$messageKeyPrefix.title",
    s"$messageKeyPrefix.heading",
    Some("Name"),
    None,
    psaName = Some("test-psa")
  )

  val formProvider = new ConfirmDeleteFormProvider()
  val form: Form[Boolean] = formProvider()

  def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () => view(form, viewModel, mode)(fakeRequest, messages)

  "ConfirmDeletePartner view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }

}
