package forms.register.company

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CompanyNameUniqueTaxReferenceFormProviderSpec extends StringFieldBehaviours {

  val form = new CompanyNameUniqueTaxReferenceFormProvider()()

  ".field1" must {

    val fieldName = "field1"
    val requiredKey = "companyNameUniqueTaxReference.error.field1.required"
    val lengthKey = "companyNameUniqueTaxReference.error.field1.length"
    val maxLength = 105

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".field2" must {

    val fieldName = "field2"
    val requiredKey = "companyNameUniqueTaxReference.error.field2.required"
    val lengthKey = "companyNameUniqueTaxReference.error.field2.length"
    val maxLength = 10

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
