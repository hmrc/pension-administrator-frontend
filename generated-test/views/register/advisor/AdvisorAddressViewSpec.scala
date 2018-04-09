package views.register.advisor

import play.api.data.Form
import controllers.register.advisor.routes
import forms.register.advisor.AdvisorAddressFormProvider
import models.NormalMode
import models.register.advisor.AdvisorAddress
import views.behaviours.QuestionViewBehaviours
import views.html.register.advisor.advisorAddress

class AdvisorAddressViewSpec extends QuestionViewBehaviours[AdvisorAddress] {

  val messageKeyPrefix = "advisorAddress"

  override val form = new AdvisorAddressFormProvider()()

  def createView = () => advisorAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => advisorAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "AdvisorAddress view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, controllers.register.advisor.routes.AdvisorAddressController.onSubmit(NormalMode).url, "field1", "field2")
  }
}
