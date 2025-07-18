/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import identifiers.register.company.MoreThanTenDirectorsId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership.MoreThanTenPartnersId
import identifiers.register.partnership.partners.PartnerNameId
import models.requests.DataRequest
import models.{Mode, PersonName, UpdateMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

import scala.concurrent.Future

trait ConfirmDeleteController extends FrontendBaseController with I18nSupport with Retrievals with Variations {

  protected def cacheConnector: UserAnswersCacheConnector

  protected def form(name: String)(implicit messages: Messages): Form[Boolean]

  protected def view: confirmDelete

  def get(vm: ConfirmDeleteViewModel, isDeleted: Boolean, redirectTo: Call, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    if (!isDeleted) {
      Future.successful(Ok(view(form(vm.name), vm, mode)))
    } else {
      Future.successful(Redirect(redirectTo))
    }

  def saveChangeFlags[A](id: TypedIdentifier[A], mode: Mode)(implicit request: DataRequest[AnyContent]): Future[JsValue] =
    if (mode == UpdateMode) {
      saveChangeFlag(mode, id).flatMap { _ =>
        val moreThanTenId = id match {
          case DirectorNameId(_) => MoreThanTenDirectorsId
          case PartnerNameId(_) => MoreThanTenPartnersId
          case _ => throw new RuntimeException("Illegal ID passed into confirm delete controller:" + id)
        }

        request.userAnswers.get(moreThanTenId) match {
          case Some(true) => saveChangeFlag(UpdateMode, moreThanTenId)
          case _ => Future.successful(request.userAnswers.json)
        }
      }
    } else {
      Future.successful(request.userAnswers.json)
    }

  def post(vm: ConfirmDeleteViewModel, id: TypedIdentifier[PersonName], postUrl: Call, mode: Mode)
          (implicit request: DataRequest[AnyContent]): Future[Result] = {

    form(vm.name).bindFromRequest().fold(
      (formWithError: Form[?]) => Future.successful(BadRequest(view(formWithError, vm, mode))),
      {
        case true =>
          id.retrieve.map { details =>
            saveChangeFlags(id, mode).flatMap { _ =>
              cacheConnector.save(id, details.copy(isDeleted = true)) map { _ =>
                Redirect(postUrl)
              }
            }
          }
        case false =>
          Future.successful(Redirect(postUrl))
      }
    )
  }

}
