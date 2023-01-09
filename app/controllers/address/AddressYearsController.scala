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

package controllers.address

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.AllowAccessActionProvider
import controllers.{Retrievals, Variations}
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.Future

trait AddressYearsController extends FrontendBaseController with Retrievals with Variations {

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def navigator: Navigator

  protected val allowAccess: AllowAccessActionProvider

  protected def view: addressYears

  protected def get(id: TypedIdentifier[AddressYears], form: Form[AddressYears], viewmodel: AddressYearsViewModel, mode: Mode)
                   (implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {

    val filledForm =
      request.userAnswers.get(id).map(form.fill).getOrElse(form)

    Future.successful(Ok(view(filledForm, viewmodel, mode)))
  }

  protected def post(id: TypedIdentifier[AddressYears], mode: Mode, form: Form[AddressYears], viewmodel: AddressYearsViewModel,
                     nav: Option[Navigator] = None)
                    (implicit request: DataRequest[AnyContent], messages: Messages): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, viewmodel, mode))),
      addressYears =>
        cacheConnector.save(request.externalId, id, addressYears).flatMap {
          answers =>
            saveChangeFlag(mode, id).map(_ =>
              Redirect(nav.getOrElse(navigator).nextPage(id, mode, UserAnswers(answers)))
            )
        }
    )
  }
}
