/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.DataCacheConnector
import identifiers.TypedIdentifier
import models.PersonDetails
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import utils.Navigator
import viewmodels.PersonDetailsViewModel

class PersonDetailsControllerSpec extends ControllerSpecBase with PersonDetailsControllerBehaviour {

  import PersonDetailsControllerSpec._

  "PersonDetailsController" must {
    behave like personDetailsController(viewModel, testId, createController(this))
  }

}

object PersonDetailsControllerSpec {

  lazy val viewModel =
    PersonDetailsViewModel(
      title = "directorDetails.title",
      heading = "directorDetails.heading",
      postCall = Call("POST", "http://www.test.com")
    )

  val testId: TypedIdentifier[PersonDetails] = new TypedIdentifier[PersonDetails] {}

  def createController(base: ControllerSpecBase)(connector: DataCacheConnector, nav: Navigator): PersonDetailsController = {
    new PersonDetailsController {
      override def appConfig: FrontendAppConfig = base.frontendAppConfig

      override def dataCacheConnector: DataCacheConnector = connector

      override def navigator: Navigator = nav

      override def messagesApi: MessagesApi = base.messagesApi
    }
  }

}
