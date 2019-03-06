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
import controllers.actions._
import controllers.{Retrievals, Variations}
import forms.ConfirmDeleteAdviserFormProvider
import identifiers.register.adviser.{AdviserNameId, ConfirmDeleteAdviserId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Adviser
import utils.{Navigator, UserAnswers}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

import scala.concurrent.Future

class ConfirmDeleteAdviserController @Inject()(
                                                val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val cacheConnector: UserAnswersCacheConnector,
                                                formProvider: ConfirmDeleteAdviserFormProvider,
                                                @Adviser navigator: Navigator
                                              ) extends FrontendController with I18nSupport with Retrievals with Variations {

  private def viewModel(name: String) = ConfirmDeleteViewModel(
    routes.ConfirmDeleteAdviserController.onSubmit(),
    controllers.routes.PsaDetailsController.onPageLoad(),
    Message("confirmDelete.adviser.title"),
    "confirmDelete.adviser.heading",
    Some(name),
    None
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.right.map { name =>
        Future.successful(Ok(confirmDelete(appConfig, formProvider(name), viewModel(name))))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      AdviserNameId.retrieve.right.map { name =>
        val form = formProvider(name)
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(confirmDelete(appConfig, formWithErrors, viewModel(name)))),
          value => {
            saveDataWithChangeFlag(value, request.externalId, mode).map(cacheMap =>
              Redirect(navigator.nextPage(ConfirmDeleteAdviserId, mode, UserAnswers(cacheMap))))
          }
        )
      }
  }

  private def saveDataWithChangeFlag(value: Boolean, id: String, mode: Mode)(implicit
                                                                             request: DataRequest[AnyContent], hc: HeaderCarrier): Future[JsValue] = {
    if (value) {
      cacheConnector.save(id, ConfirmDeleteAdviserId, value).flatMap(_ =>
        saveChangeFlag(mode, ConfirmDeleteAdviserId)
      )
    } else {
      cacheConnector.save(id, ConfirmDeleteAdviserId, value)
    }
  }
}