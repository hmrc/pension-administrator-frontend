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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.{Retrievals, Variations}
import controllers.actions._
import forms.register.adviser.AdviserDetailsFormProvider
import identifiers.register.adviser.{AdviserDetailsId, AdviserNameId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.{Navigator, UserAnswers}
import views.html.register.adviser.adviserDetails

import scala.concurrent.Future

class AdviserDetailsController @Inject()(
                                          appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val cacheConnector: UserAnswersCacheConnector,
                                          @Adviser navigator: Navigator,
                                          authenticate: AuthAction,
                                          allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AdviserDetailsFormProvider
                                        ) extends FrontendController with I18nSupport with Variations with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.right.map { adviserName =>
        val preparedForm = request.userAnswers.get(AdviserDetailsId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(adviserDetails(appConfig, preparedForm, mode, adviserName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.right.map { adviserName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(adviserDetails(appConfig, formWithErrors, mode, adviserName))),
          value =>
            cacheConnector.save(request.externalId, AdviserDetailsId, value).flatMap(cacheMap =>
              saveChangeFlag(mode, AdviserDetailsId).map(_ =>
                Redirect(navigator.nextPage(AdviserDetailsId, mode, UserAnswers(cacheMap))))
            )
        )
      }
  }
}
