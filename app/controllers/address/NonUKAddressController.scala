/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.{RegistrationConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import identifiers.TypedIdentifier
import identifiers.register.RegistrationInfoId
import models._
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AnyContent, Request, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.Future

trait NonUKAddressController extends FrontendController with Retrievals with I18nSupport {

  protected def appConfig: FrontendAppConfig

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def registrationConnector: RegistrationConnector

  protected def navigator: Navigator

  protected val form: Form[Address]

  protected def createView(appConfig: FrontendAppConfig, preparedForm: Form[_], viewModel: ManualAddressViewModel)(
    implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable = () =>
    nonukAddress(appConfig, preparedForm, viewModel)(request, messages)

  protected def get(id: TypedIdentifier[TolerantAddress], viewModel: ManualAddressViewModel
                   )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).fold(form)(v => form.fill(v.toAddress))
    val view = createView(appConfig, preparedForm, viewModel)

    Future.successful(Ok(view()))
  }

  protected def post(name: String, id: TypedIdentifier[TolerantAddress], viewModel: ManualAddressViewModel)(
    implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithError: Form[_]) => {
        val view = createView(appConfig, formWithError, viewModel)
        Future.successful(BadRequest(view()))
      },
      address => {
        dataCacheConnector.save(request.externalId, id, address.toTolerantAddress).flatMap { cacheMap =>
          val nextPageCall = navigator.nextPage(id, NormalMode, UserAnswers(cacheMap))
          val areYouInUKCallURL = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode).url
          (if (nextPageCall.url == areYouInUKCallURL) {
            Future.successful(())
          } else {
            registrationConnector.registerWithNoIdOrganisation(name, address).flatMap { registrationInfo =>
              dataCacheConnector.save(request.externalId, RegistrationInfoId, registrationInfo)
            }
          }).map(_=>Redirect(nextPageCall))
        }
      }
    )
  }
}
