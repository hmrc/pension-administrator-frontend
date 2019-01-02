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

import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import models.{BusinessDetails, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{BusinessDetailsViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.businessDetails

//scalastyle:off magic.number

class BusinessDetailsViewSpec extends QuestionViewBehaviours[BusinessDetails] {

  val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "businessDetails.error.companyName.required",
      companyNameLengthMsg = "businessDetails.error.companyName.length",
      companyNameInvalidMsg = "businessDetails.error.companyName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = Some("businessDetails.error.utr.required"),
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )

  val messageKeyPrefix = "businessDetails"

  val form = new BusinessDetailsFormProvider(isUK=true)(formModel)

  val viewModel = BusinessDetailsViewModel(
    postCall = Call("GET", "/"),
    title = Message("businessDetails.title"),
    heading = Message("businessDetails.heading"),
    companyNameLabel = Message("businessDetails.companyName"),
    companyNameHint = Message("businessDetails.companyName.hint"),
    utrLabel = Message("businessDetails.utr"),
    utrHint = Message("businessDetails.utr.hint")
  )

  def createView: () => HtmlFormat.Appendable = () => businessDetails(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => businessDetails(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyDetailsController.onSubmit(NormalMode).url,
      "companyName",
      "utr")

    behave like pageWithLabel(createViewUsingForm, "companyName", messages("businessDetails.companyName"))

    behave like pageWithLabel(createViewUsingForm, "utr", messages("businessDetails.utr"))

  }


}
