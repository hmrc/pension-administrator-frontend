package forms.register.partnership

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ConfirmPartnershipDetailsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "confirmPartnershipDetails.error.required"
  val invalidKey = "error.boolean"

  val form = new ConfirmPartnershipDetailsFormProvider()()

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
