package forms.register

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class VatFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "vat.error.required"
  val invalidKey = "error.boolean"

  val form = new VatFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
