/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, FakeAllowAccessProvider}
import identifiers.TypedIdentifier
import models.{NormalMode, PersonName}
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, MessagesControllerComponents}
import utils.Navigator
import viewmodels.CommonFormWithHintViewModel
import views.html.personName

import scala.concurrent.ExecutionContext

class PersonNameControllerSpec extends ControllerSpecBase with PersonNameControllerBehaviour {

  private val psaName = "test name"

  private lazy val viewModel =
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
  "PersonNameController" must {
    behave like personNameController(viewModel, testId, createController(this))
  }



  def createController(base: ControllerSpecBase)(connector: UserAnswersCacheConnector, nav: Navigator): PersonNameController = {
    new PersonNameController {

      override def cacheConnector: UserAnswersCacheConnector = connector

      override def navigator: Navigator = nav

      override def messagesApi: MessagesApi = base.messagesApi

      override val allowAccess: AllowAccessActionProvider = FakeAllowAccessProvider(config = frontendAppConfig)

      implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      override val controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents

      val view: personName = app.injector.instanceOf[personName]
    }
  }

}




