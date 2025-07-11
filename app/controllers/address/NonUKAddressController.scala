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

package controllers.address

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import identifiers.TypedIdentifier
import identifiers.register.RegistrationInfoId
import models.InternationalRegion.RestOfTheWorld
import models._
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AnyContent, Request, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.{ExecutionContext, Future}

trait NonUKAddressController extends FrontendBaseController with Retrievals with I18nSupport {

  implicit val executionContext: ExecutionContext

  protected def dataCacheConnector: UserAnswersCacheConnector

  protected def registrationConnector: RegistrationConnector

  protected def navigator: Navigator

  protected val form: Form[Address]

  protected val countryOptions: CountryOptions

  protected def view: nonukAddress

  protected def createView(preparedForm: Form[?], viewModel: ManualAddressViewModel)
                          (implicit request: Request[?], messages: Messages): () => HtmlFormat.Appendable = () =>
    view(preparedForm, viewModel)(request, messages)

  protected def get(id: TypedIdentifier[TolerantAddress], viewModel: ManualAddressViewModel)
                   (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val preparedForm = request.userAnswers.get(id).fold(form)(v => form.fill(v.toPrepopAddress))
    val view = createView(preparedForm, viewModel)

    Future.successful(Ok(view()))
  }

  protected def post(name: String, id: TypedIdentifier[TolerantAddress], viewModel: ManualAddressViewModel, legalStatus: RegistrationLegalStatus)
                    (implicit request: DataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      (formWithError: Form[?]) => {
        val view = createView(formWithError, viewModel)
        Future.successful(BadRequest(view()))
      },
      address => {
        if (address.country.equals("GB")) {
          redirectUkAddress(address, id)
        } else {
          val cacheMap = dataCacheConnector.save(id, address.toTolerantAddress)
          val resultAfterRegisterWithoutID = cacheMap.flatMap { _ =>
            countryOptions.regions(address.country) match {
              case RestOfTheWorld =>
                dataCacheConnector.remove(RegistrationInfoId).map(_=>())
              case _ =>
                registrationConnector.registerWithNoIdOrganisation(name, address, legalStatus).flatMap { registrationInfo =>
                  dataCacheConnector.save(RegistrationInfoId, registrationInfo)
                }.map(_ => ())
            }
          }

          cacheMap.flatMap { cm =>
            resultAfterRegisterWithoutID.map { _ =>
              Redirect(navigator.nextPage(id, NormalMode, UserAnswers(cm)))
            }
          }
        }
      }
    )
  }

  def redirectUkAddress(
                        address: Address,
                        id: TypedIdentifier[TolerantAddress]
                       )(implicit hc: HeaderCarrier): Future[Result] =
    for {
      cacheMap <- dataCacheConnector.save(id, address.toTolerantAddress)
    } yield {
      Redirect(navigator.nextPage(id, NormalMode, UserAnswers(cacheMap)))
    }

}
