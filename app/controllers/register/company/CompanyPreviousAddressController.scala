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
import identifiers.register.company.CompanyPreviousAddressId
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import views.html.register.company.companyPreviousAddress

import scala.concurrent.Future

class CompanyPreviousAddressController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        @RegisterCompany navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddressFormProvider,
                                        countryOptions: CountryOptions
                                      ) extends FrontendController with I18nSupport {

  private val form: Form[Address] = formProvider("error.country.invalid")

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(CompanyPreviousAddressId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(companyPreviousAddress(appConfig, preparedForm, mode, countryOptions.options))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(companyPreviousAddress(appConfig, formWithErrors, mode, countryOptions.options))),
        (value) =>
          dataCacheConnector.save(request.externalId, CompanyPreviousAddressId, value).map(cacheMap =>
            Redirect(navigator.nextPage(CompanyPreviousAddressId, mode)(new UserAnswers(cacheMap))))
    )
  }
}
