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

package controllers.register.adviser

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{DataRequiredAction, AuthAction, DataRetrievalAction}
import forms.register.adviser.AdviserNameFormProvider
import identifiers.UpdateContactAddressId
import identifiers.register.adviser.AdviserNameId
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Action}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Adviser
import utils.{Navigator, UserAnswers}
import views.html.register.adviser.adviserName

import scala.concurrent.{Future, ExecutionContext}

class AdviserNameController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       authenticate: AuthAction,
                                       @Adviser navigator: Navigator,
                                       getData: DataRetrievalAction,
                                       requiredData: DataRequiredAction,
                                       formProvider: AdviserNameFormProvider,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: adviserName
                                     )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AdviserNameId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty

      Future.successful(Ok(view(
        preparedForm,
        mode,
        if (displayReturnLink) psaName() else None
      )))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requiredData).async {
    implicit request =>

      val displayReturnLink = request.userAnswers.get(UpdateContactAddressId).isEmpty

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(
            formWithErrors,
            mode,
            if (displayReturnLink) psaName() else None
          ))),
        value => {
          dataCacheConnector.save(request.externalId, AdviserNameId, value).map(
            cacheMap =>
              Redirect(navigator.nextPage(AdviserNameId, mode, UserAnswers(cacheMap)))
          )
        }
      )
  }
}
