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

package controllers.register.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.UTRController
import forms.UTRFormProvider
import identifiers.register.BusinessTypeId
import identifiers.register.company.CompanyUTRId
import javax.inject.Inject
import models.Mode
import models.register.BusinessType
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import utils.Navigator
import utils.annotations.RegisterCompany
import viewmodels.Message

import scala.concurrent.ExecutionContext

class CompanyUTRController @Inject()(override val appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     override val cacheConnector: UserAnswersCacheConnector,
                                     @RegisterCompany override val navigator: Navigator,
                                     authenticate: AuthAction,
                                     override val allowAccess: AllowAccessActionProvider,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction
                                    ) extends UTRController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessTypeId.retrieve.right.map { businessType =>
        get(CompanyUTRId, orgType(businessType), href(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

            BusinessTypeId.retrieve.right.map { businessType =>
              post(CompanyUTRId, orgType(businessType), href(mode), mode)
            }
  }

  def href(mode: Mode): Call = routes.CompanyUTRController.onSubmit(mode)
  def orgType(businessType: BusinessType): String = Message(s"businessType.${businessType.toString}").toLowerCase()

}
