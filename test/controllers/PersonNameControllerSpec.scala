/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.FakeAllowAccessProvider
import identifiers.TypedIdentifier
import models.{NormalMode, PersonName}
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import utils.Navigator
import viewmodels.CommonFormWithHintViewModel

class PersonNameControllerSpec extends ControllerSpecBase with PersonNameControllerBehaviour {

  import PersonNameControllerSpec._

  "PersonNameController" must {
    behave like personNameController(viewModel, testId, createController(this))
  }

}

object PersonNameControllerSpec {

  val psaName = "test name"

  lazy val viewModel =
    CommonFormWithHintViewModel(
      postCall = Call("POST", "http://www.test.com"),
      title = "directorName.heading",
      heading = "directorName.heading",
      None,
      None,
      NormalMode,
      psaName
    )

  val testId: TypedIdentifier[PersonName] = new TypedIdentifier[PersonName] {}

  def createController(base: ControllerSpecBase)(connector: UserAnswersCacheConnector, nav: Navigator): PersonNameController = {
    new PersonNameController {
      override def appConfig: FrontendAppConfig = base.frontendAppConfig

      override def cacheConnector: UserAnswersCacheConnector = connector

      override def navigator: Navigator = nav

      override def messagesApi: MessagesApi = base.messagesApi

      override val allowAccess = FakeAllowAccessProvider()
    }
  }

}



