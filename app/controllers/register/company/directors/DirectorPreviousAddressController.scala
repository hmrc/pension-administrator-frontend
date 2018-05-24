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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.AddressFormProvider
import identifiers.register.company.directors.{DirectorPreviousAddressId, DirectorPreviousAddressListId}
import models.{Address, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class DirectorPreviousAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val dataCacheConnector: DataCacheConnector,
                                                  @CompanyDirector override val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: AddressFormProvider,
                                                  countryOptions: CountryOptions,
                                                  val auditService: AuditService) extends ManualAddressController with Retrievals {

  override protected val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) {
        directorName =>
          get(DirectorPreviousAddressId(index), DirectorPreviousAddressListId(index), addressViewModel(mode, index, directorName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) {
        directorName =>
          post(DirectorPreviousAddressId(index), DirectorPreviousAddressListId(index), addressViewModel(mode, index, directorName), mode)
      }
  }

  private def addressViewModel(mode: Mode, index: Index, directorName: String) =
    ManualAddressViewModel(
      routes.DirectorPreviousAddressController.onSubmit(mode, index),
      countryOptions.options,
      Message("directorPreviousAddress.title"),
      Message("directorPreviousAddress.heading"),
      Some(Message(directorName))
    )

}

/*
class DirectorPreviousAddressController @Inject()(
                                                   appConfig: FrontendAppConfig,
                                                   override val messagesApi: MessagesApi,
                                                   dataCacheConnector: DataCacheConnector,
                                                   @CompanyDirector navigator: Navigator,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressFormProvider,
                                                   countryOptions: CountryOptions
                                                 ) extends FrontendController with Retrievals with I18nSupport {

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

}
*/
