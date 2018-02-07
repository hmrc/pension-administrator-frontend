package views.$routeFile$

import play.api.data.Form
import forms.$routeFile$.$className$FormProvider
import models.NormalMode
import models.$routeFile$.$className$
import views.behaviours.ViewBehaviours
import views.html.$routeFile$.$className;format="decap"$

class $className$ViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "$className;format="decap"$"

  val form = new $className$FormProvider()()

  def createView = () => $className;format="decap"$(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => $className;format="decap"$(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "$className$ view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "$className$ view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- $className$.options) {
          assertContainsRadioButton(doc, s"value-\${option.value}", "value", option.value, false)
        }
      }
    }

    for(option <- $className$.options) {
      s"rendered with a value of '\${option.value}'" must {
        s"have the '\${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"\${option.value}"))))
          assertContainsRadioButton(doc, s"value-\${option.value}", "value", option.value, true)

          for(unselectedOption <- $className$.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-\${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
