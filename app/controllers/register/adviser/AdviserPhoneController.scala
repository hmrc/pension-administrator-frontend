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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.PhoneController
import forms.PhoneFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser.{AdviserPhoneId, AdviserNameId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import utils.Navigator
import utils.annotations.{Adviser, NoRLSCheck}
import viewmodels.{Message, CommonFormWithHintViewModel}
import views.html.phone

import scala.concurrent.ExecutionContext

class AdviserPhoneController @Inject()(@Adviser val navigator: Navigator,
                                       val appConfig: FrontendAppConfig,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       @NoRLSCheck val allowAccess: AllowAccessActionProvider,
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
        get(AdviserPhoneId, form, viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserPhoneId, mode, form, viewModel(mode, request.userAnswers.get(UpdateContactAddressId).isEmpty))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))

  private def viewModel(mode: Mode, displayReturnLink:Boolean)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.AdviserPhoneController.onSubmit(mode),
      title = Message("phone.title", Message("theAdviser")),
      heading = Message("phone.title", entityName),
      mode = mode,
      entityName = entityName,
      displayReturnLink = displayReturnLink
    )
}
