/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.partnership

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.BusinessNameController
import forms.BusinessNameFormProvider
import play.api.mvc.{Call, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import views.html.register.businessName

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnershipNameController @Inject()(
                                       override val cacheConnector: UserAnswersCacheConnector,
                                       @Partnership override val navigator: Navigator,
                                       override val authenticate: AuthAction,
                                       override val allowAccess: AllowAccessActionProvider,
                                       override val getData: DataRetrievalAction,
                                       override val requireData: DataRequiredAction,
                                       formProvider: BusinessNameFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: businessName
                                     )(implicit val executionContext: ExecutionContext) extends BusinessNameController {

  def href: Call = routes.PartnershipNameController.onSubmit

  override val form = formProvider(
    requiredKey = "partnershipName.error.required",
    invalidKey = "partnershipName.error.invalid",
    lengthKey = "partnershipName.error.length")
}
