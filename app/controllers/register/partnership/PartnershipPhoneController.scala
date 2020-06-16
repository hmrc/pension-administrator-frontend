/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.PhoneController
import forms.PhoneFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipPhoneId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

import scala.concurrent.ExecutionContext

class PartnershipPhoneController @Inject()(@Partnership val navigator: Navigator,
                                           val appConfig: FrontendAppConfig,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           authenticate: AuthAction,
                                           val allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: PhoneFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: phone
                                          )(implicit val executionContext: ExecutionContext) extends PhoneController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(PartnershipPhoneId, form, viewModel(mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipPhoneId, mode, form, viewModel(mode))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).fold(Message("thePartnership"))(identity)

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipPhoneController.onSubmit(mode),
      title = Message("phone.title", Message("thePartnership")),
      heading = Message("phone.title", entityName),
      mode = mode,
      entityName = entityName
    )
}
