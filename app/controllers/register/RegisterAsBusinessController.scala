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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.RegisterAsBusinessFormProvider
import identifiers.register.RegisterAsBusinessId
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{AuthenticationWithLowConfidence, Register}
import utils.{Navigator, UserAnswers}
import views.html.register.registerAsBusiness

import scala.concurrent.{ExecutionContext, Future}

class RegisterAsBusinessController @Inject()(
  appConfig: FrontendAppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  @AuthenticationWithLowConfidence authenticateLow: AuthAction,
  allowAccess: AllowAccessActionProvider,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cache: UserAnswersCacheConnector,
  @Register navigator: Navigator
)(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  def onPageLoad(mode:Mode): Action[AnyContent] = (authenticateLow andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(RegisterAsBusinessId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(registerAsBusiness(appConfig, preparedForm))

  }

  def onSubmit(mode:Mode): Action[AnyContent] = (authenticateLow andThen getData andThen requireData).async {
    implicit request=>

      form.bindFromRequest().fold(
        errors =>
          Future.successful(BadRequest(registerAsBusiness(appConfig, errors))),
        isBusiness => {
          cache.save(request.externalId, RegisterAsBusinessId, isBusiness).map {
            newCache =>
              Redirect(navigator.nextPage(RegisterAsBusinessId, NormalMode, UserAnswers(newCache)))
          }
        }
      )

  }

}
