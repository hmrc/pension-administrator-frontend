package forms.register

import forms.FormErrorHelper
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class VatFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("vat.error.required")
    )
}
