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

package controllers.address

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.AllowAccessActionProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{Mode, TolerantAddress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

trait PostcodeLookupController extends FrontendBaseController with Retrievals with I18nSupport {
  implicit val ec = play.api.libs.concurrent.Execution.defaultContext
  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  protected def addressLookupConnector: AddressLookupConnector

  protected def navigator: Navigator

  protected def form: Form[String]

  protected val allowAccess: AllowAccessActionProvider

  private val invalidPostcode: Message = "error.postcode.failed"
  private val noResults: Message = "error.postcode.noResults"

  protected def get(viewmodel: PostcodeLookupViewModel, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {

    Future.successful(Ok(postcodeLookup(appConfig, form, viewmodel, mode)))
  }

  protected def post(
                      id: TypedIdentifier[Seq[TolerantAddress]],
                      viewmodel: PostcodeLookupViewModel,
                      mode: Mode,
                      invalidPostcode: Message = invalidPostcode,
                      noResults: Message = noResults
                    )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful {
          BadRequest(postcodeLookup(appConfig, formWithErrors, viewmodel, mode))
        },
      lookupPostcode(id, viewmodel, invalidPostcode, noResults, mode)
    )
  }

  private def lookupPostcode(
                              id: TypedIdentifier[Seq[TolerantAddress]],
                              viewmodel: PostcodeLookupViewModel,
                              invalidPostcode: Message,
                              noResults: Message,
                              mode: Mode
                            )(postcode: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {

    addressLookupConnector.addressLookupByPostCode(postcode).flatMap {

      case Nil => Future.successful(Ok(postcodeLookup(appConfig, formWithError(noResults), viewmodel, mode)))

      case addresses =>
        cacheConnector.save(
          request.externalId,
          id,
          addresses
        ).map {
          json =>
            Redirect(navigator.nextPage(id, mode, UserAnswers(json)))
        }
    } recoverWith {

      case _ =>
        Future.successful(BadRequest(postcodeLookup(appConfig, formWithError(invalidPostcode), viewmodel, mode)))

    }
  }


  protected def formWithError(message: Message)(implicit request: DataRequest[AnyContent]): Form[String] = {
    form.withError("value", message.resolve)
  }
}
