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

package controllers.register

import com.google.inject.Singleton
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.register.DeclarationId
import javax.inject.Inject
import models.{Mode, NormalMode, UserType}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Navigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      authenticate: AuthAction,
                                      allowAccess: AllowAccessActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      @Register navigator: Navigator,
                                      formProvider: DeclarationFormProvider,
                                      dataCacheConnector: UserAnswersCacheConnector
                                     )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode:Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      request.user.userType match {
        case UserType.Individual =>
          Future.successful(Ok(declaration(appConfig, form, individual.routes.WhatYouWillNeedController.onPageLoad())))
        case UserType.Organisation =>
          Future.successful(Ok(declaration(appConfig, form, company.routes.WhatYouWillNeedController.onPageLoad())))
      }
  }

  def onSubmit(mode:Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          request.user.userType match {
            case UserType.Individual =>
              Future.successful(BadRequest(
                declaration(appConfig, errors, individual.routes.WhatYouWillNeedController.onPageLoad())))

            case UserType.Organisation =>
              Future.successful(BadRequest(
                declaration(appConfig, errors, company.routes.WhatYouWillNeedController.onPageLoad())))
          },
        success => dataCacheConnector.save(request.externalId, DeclarationId, success).map { cacheMap =>
          Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
        }
      )
  }
}
