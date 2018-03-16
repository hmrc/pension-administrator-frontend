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

package controllers.register.company.directors

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.directors.DirectorAddressId
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.CompanyDirector
import utils.{CountryOptions, Navigator, UserAnswers}
import views.html.register.company.directors.directorAddress

import scala.concurrent.Future

class DirectorAddressController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: DataCacheConnector,
                                           @CompanyDirector navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: AddressFormProvider,
                                           countryOptions: CountryOptions
                                         ) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(DirectorAddressId(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      retrieveDirectorName(index) { directorName =>
        Future.successful(Ok(directorAddress(appConfig, preparedForm, mode, index, directorName, countryOptions.options)))
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorAddress(appConfig, formWithErrors, mode, index, directorName, countryOptions.options))),
          (value) =>
            dataCacheConnector.save(request.externalId, DirectorAddressId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(DirectorAddressId(index), mode)(new UserAnswers(cacheMap))))
        )
      }
  }
}
