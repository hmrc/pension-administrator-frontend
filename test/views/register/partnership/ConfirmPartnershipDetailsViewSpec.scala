package views.register.partnership

import play.api.data.Form
import controllers.register.partnership.routes
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.partnership.confirmPartnershipDetails

class ConfirmPartnershipDetailsViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "confirmPartnershipDetails"

  val form = new ConfirmPartnershipDetailsFormProvider()()

  def createView = () => confirmPartnershipDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => confirmPartnershipDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "ConfirmPartnershipDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.ConfirmPartnershipDetailsController.onSubmit(NormalMode).url)
  }
}
