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

package controllers.register

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.{Journey, JourneyType}
import forms.register.DeclarationFormProvider
import identifiers.register.DeclarationFitAndProperId
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import utils.{Navigator, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.Future

class DeclarationFitAndProperController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         navigator: Navigator,
                                         formProvider: DeclarationFormProvider,
                                         dataCacheConnector: DataCacheConnector,
                                         journey: Journey) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
      implicit request =>
        journey.withJourneyType {
          case JourneyType.Individual =>
            Future.successful(Ok(declarationFitAndProper(appConfig, form, controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad())))
          case JourneyType.Company =>
            Future.successful(Ok(declarationFitAndProper(appConfig, form, controllers.register.company.routes.WhatYouWillNeedController.onPageLoad())))
        }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors => journey.withJourneyType {
          case JourneyType.Individual =>
            Future.successful(BadRequest(declarationFitAndProper(appConfig, errors, controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad())))
          case JourneyType.Company =>
            Future.successful(BadRequest(declarationFitAndProper(appConfig, errors, controllers.register.company.routes.WhatYouWillNeedController.onPageLoad())))
        },
        success => dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).map { cacheMap =>
          Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode)(UserAnswers(cacheMap)))
        }
      )
  }


}
