package views.register

import play.api.data.Form
import controllers.register.routes
import forms.register.VatFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import views.html.register.vat

class VatViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "vat"

  val form = new VatFormProvider()()

  def createView = () => vat(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => vat(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "Vat view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.VatController.onSubmit(NormalMode).url)
  }
}
