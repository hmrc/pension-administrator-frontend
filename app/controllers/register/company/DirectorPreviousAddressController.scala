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

package controllers.register.company

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.{DirectorDetailsId, DirectorPreviousAddressId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CountryOptions, Navigator, UserAnswers}
import views.html.register.company.directorPreviousAddress

import scala.concurrent.Future

class DirectorPreviousAddressController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddressFormProvider,
                                        countryOptions: CountryOptions
                                      ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        val preparedForm = request.userAnswers.get(DirectorPreviousAddressId(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(directorPreviousAddress(appConfig, preparedForm, mode, index, directorName, countryOptions.options)))
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorPreviousAddress(appConfig, formWithErrors, mode, index, directorName, countryOptions.options))),
          (value) =>
            dataCacheConnector.save(request.externalId, DirectorPreviousAddressId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(DirectorPreviousAddressId(index), mode)(new UserAnswers(cacheMap))))
        )
      }
  }

  def retrieveDirectorName(index: Int)(block: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] = {
      request.userAnswers.get(DirectorDetailsId(index)) match {
        case Some(value) =>
          block(value.fullName)
        case _ =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }
}
