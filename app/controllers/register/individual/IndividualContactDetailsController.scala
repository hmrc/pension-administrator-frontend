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

package controllers.register.individual

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.individual.IndividualContactDetailsId
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Individual
import viewmodels.{ContactDetailsViewModel, Message}


class IndividualContactDetailsController @Inject()(
                                                    @Individual override val navigator: Navigator,
                                                    override val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    override val cacheConnector: UserAnswersCacheConnector,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: ContactDetailsFormProvider
                                                  ) extends controllers.ContactDetailsController {

  private def viewmodel(mode: Mode) = ContactDetailsViewModel(
    postCall = routes.IndividualContactDetailsController.onSubmit(mode),
    title = Message("contactDetails.individual.title"),
    heading = Message("contactDetails.individual.heading"),
    body = Some(Message("contactDetails.body"))
  )

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(IndividualContactDetailsId, form, viewmodel(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      post(IndividualContactDetailsId, mode, form, viewmodel(mode), savePsaEmail = true)
  }
}
