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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.PhoneController
import forms.PhoneFormProvider
import identifiers.register.partnership.partners.{PartnerNameId, PartnerPhoneId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{CommonFormWithHintViewModel, Message}

class PartnerPhoneController @Inject()(@PartnershipPartner val navigator: Navigator,
                                       val appConfig: FrontendAppConfig,
                                       val messagesApi: MessagesApi,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       val allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PhoneFormProvider
                                      ) extends PhoneController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(PartnerPhoneId(index), form, viewModel(mode, index, entityName(index)))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnerPhoneId(index), mode, form, viewModel(mode, index, entityName(index)))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerPhoneController.onSubmit(mode, index),
      title = Message("phone.title", Message("thePartner").resolve),
      heading = Message("phone.title", partnerName),
      mode = mode,
      entityName = partnerName
    )
}
