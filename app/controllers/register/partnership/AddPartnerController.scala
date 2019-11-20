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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.AddEntityController
import forms.register.AddEntityFormProvider
import identifiers.register.partnership.AddPartnersId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{EntityViewModel, Message, Person}
import views.html.register.addEntity

class AddPartnerController @Inject()(
                                      override val appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      override val cacheConnector: UserAnswersCacheConnector,
                                      @PartnershipPartner override val navigator: Navigator,
                                      authenticate: AuthAction,
                                      allowAccess: AllowAccessActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: AddEntityFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: addEntity
                                    ) extends AddEntityController with Retrievals {

  private val form: Form[Boolean] = formProvider()

  private def viewmodel(partners: Seq[Person], mode: Mode)(implicit request: DataRequest[AnyContent]) = EntityViewModel(
    postCall = routes.AddPartnerController.onSubmit(mode),
    title = Message("addPartners.title"),
    heading = Message("addPartners.heading"),
    entities = partners,
    maxLimit = appConfig.maxPartners,
    entityType = Message("addPartners.entityType"),
    psaName = psaName()
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val partners: Seq[Person] = request.userAnswers.allPartnersAfterDelete(mode)
      get(AddPartnersId, form, viewmodel(partners, mode), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partners: Seq[Person] = request.userAnswers.allPartnersAfterDelete(mode)
      post(AddPartnersId, form, viewmodel(partners, mode), mode)
  }

}
