/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.adviser.routes._
import controllers.routes._
import controllers.{Retrievals, Variations}
import forms.register.adviser.ConfirmDeleteAdviserFormProvider
import identifiers.register.adviser._
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.NoRLSCheck
import utils.{Navigator, UserAnswers, annotations}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmDeleteAdviserController @Inject()(
                                                authenticate: AuthAction,
                                                @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val cacheConnector: UserAnswersCacheConnector,
                                                formProvider: ConfirmDeleteAdviserFormProvider,
                                                @annotations.Variations navigator: Navigator,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: confirmDelete
                                              )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals
    with Variations {

  private def viewModel(name: String)
                       (implicit request: DataRequest[AnyContent]) =
    ConfirmDeleteViewModel(
      postUrl = ConfirmDeleteAdviserController.onSubmit(),
      cancelUrl = PsaDetailsController.onPageLoad(),
      title = Message("confirmDelete.adviser.title"),
      heading = "confirmDelete.adviser.heading",
      name = name,
      psaName = psaName()
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get(AdviserNameId) match {
          case Some(name) =>
            Future.successful(Ok(view(formProvider(name), viewModel(name), mode)))
          case _ =>
            Future.successful(Redirect(AdviserAlreadyDeletedController.onPageLoad()))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        AdviserNameId.retrieve.map { name =>
          val form = formProvider(name)
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, viewModel(name), mode))),
            value => {
              cacheConnector.save(request.externalId, ConfirmDeleteAdviserId, value) flatMap {
                cacheMap =>
                  deleteAdviserAndSetChangeFlag(value, UserAnswers(cacheMap), mode) map {
                    updatedCacheMap =>
                      Redirect(navigator.nextPage(ConfirmDeleteAdviserId, mode, UserAnswers(updatedCacheMap)))
                  }
              }
            }
          )
        }
    }

  private def deleteAdviserAndSetChangeFlag(value: Boolean, userAnswers: UserAnswers, mode: Mode)
                                           (implicit request: DataRequest[AnyContent]): Future[JsValue] =
    if (value) {
      val updatedAnswers =
        userAnswers
          .removeAllOf(
            List(
              AdviserNameId,
              AdviserEmailId,
              AdviserPhoneId,
              AdviserAddressId,
              AdviserAddressListId,
              AdviserAddressPostCodeLookupId
            )
          )
          .asOpt
          .getOrElse(userAnswers)

      cacheConnector.upsert(request.externalId, updatedAnswers.json) flatMap {
        _ =>
          saveChangeFlag(mode, ConfirmDeleteAdviserId)
      }
    } else {
      Future.successful(userAnswers.json)
    }
}
