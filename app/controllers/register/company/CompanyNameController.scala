/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.company

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.BusinessNameController
import forms.BusinessNameFormProvider
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Call, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import views.html.register.businessName

import scala.concurrent.ExecutionContext

class CompanyNameController @Inject()(override val appConfig: FrontendAppConfig,
                                      override val cacheConnector: UserAnswersCacheConnector,
                                      @RegisterCompany override val navigator: Navigator,
                                      override val authenticate: AuthAction,
                                      override val allowAccess: AllowAccessActionProvider,
                                      override val getData: DataRetrievalAction,
                                      override val requireData: DataRequiredAction,
                                      formProvider: BusinessNameFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: businessName
                                     )(implicit val executionContext: ExecutionContext) extends BusinessNameController {

  def href: Call = routes.CompanyNameController.onSubmit()

  val form: Form[String] = formProvider()
}
