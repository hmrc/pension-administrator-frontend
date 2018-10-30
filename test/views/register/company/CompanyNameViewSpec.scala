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

import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import models.{BusinessDetails, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import viewmodels.{OrganisationNameViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.organisationName

class CompanyNameViewSpec extends QuestionViewBehaviours[BusinessDetails] {

  private val messageKeyPrefix = "companyName"

  private lazy val viewModel =
    OrganisationNameViewModel(
      title = "companyName.title",
      heading = Message("companyName.heading"),
      postCall = Call("POST", "http://www.test.com")
    )

  protected val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "companyName.error.required",
      companyNameLengthMsg = "companyName.error.length",
      companyNameInvalidMsg = "companyName.error.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = None,
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )

  val form = new BusinessDetailsFormProvider(isUK=false)(formModel)

  private def createView = () => organisationName(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => organisationName(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  "Company Name view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSubmitButton(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyDetailsController.onSubmit(NormalMode).url,
      "companyName")

    behave like pageWithLabel(createViewUsingForm, "companyName", messages("companyName.heading"))


  }

}