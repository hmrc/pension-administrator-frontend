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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.NonUKBusinessTypeFormProvider
import identifiers.register.NonUKBusinessTypeId
import javax.inject.Inject
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.nonUKBusinessType

import scala.concurrent.{ExecutionContext, Future}

class NonUKBusinessTypeController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: UserAnswersCacheConnector,
                                        @Register navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: NonUKBusinessTypeFormProvider
                                      )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode:Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NonUKBusinessTypeId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(nonUKBusinessType(appConfig, preparedForm))
  }

  def onSubmit(mode:Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(nonUKBusinessType(appConfig, formWithErrors))),
        value =>
          dataCacheConnector.save(request.externalId, NonUKBusinessTypeId, value).map(cacheMap =>
            Redirect(navigator.nextPage(NonUKBusinessTypeId, NormalMode, UserAnswers(cacheMap))))
      )
  }

}
