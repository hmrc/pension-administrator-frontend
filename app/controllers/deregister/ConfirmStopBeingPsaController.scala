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

package controllers.deregister

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.deregister.ConfirmStopBeingPsaFormProvider
import identifiers.deregister.ConfirmStopBeingPsaId
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.deregister.confirmStopBeingPsa

import scala.concurrent.{ExecutionContext, Future}

class ConfirmStopBeingPsaController  @Inject()(
                                         val appConfig: FrontendAppConfig,
                                         val auth: AuthAction,
                                         val messagesApi: MessagesApi,
                                         val formProvider: ConfirmStopBeingPsaFormProvider,
                                         val userAnswersCacheConnector: UserAnswersCacheConnector,
                                         val getData: DataRetrievalAction,
                                         val requireData: DataRequiredAction
                                       )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (auth andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(ConfirmStopBeingPsaId).fold(form)(form.fill(_))
      Future.successful(Ok(confirmStopBeingPsa(appConfig, preparedForm, "")))
  }

  def onSubmit: Action[AnyContent] = (auth andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(

        (formWithErrors: Form[Boolean]) =>
          Future.successful(BadRequest(confirmStopBeingPsa(appConfig, formWithErrors, ""))),

        value => {
          userAnswersCacheConnector.save(request.externalId, ConfirmStopBeingPsaId, value).map(
            cacheMap =>
              Redirect(controllers.routes.PsaDetailsController.onPageLoad())
          )
        }
      )
  }
}
