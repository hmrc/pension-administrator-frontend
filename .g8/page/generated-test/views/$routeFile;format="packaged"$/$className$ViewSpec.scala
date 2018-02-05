package views.$routeFile$

import play.api.data.Form
import controllers.routes
import views.behaviours.ViewBehaviours
import models.NormalMode
import views.html.$routeFile$.$className;format="decap"$

class $className$ViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "$className;format="decap"$"

  def createView = () => $className;format="decap"$(frontendAppConfig)(fakeRequest, messages)

  "$className$ view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }
}
