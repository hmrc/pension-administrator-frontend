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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.company.directors.{CompanyDirectorAddressPostCodeLookupId, DirectorDetailsId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.Future

class CompanyDirectorAddressPostCodeLookupController @Inject()(
                                                                override val appConfig: FrontendAppConfig,
                                                                override val cacheConnector: DataCacheConnector,
                                                                override val addressLookupConnector: AddressLookupConnector,
                                                                @CompanyDirector override val navigator: Navigator,
                                                                override val messagesApi: MessagesApi,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: PostCodeLookupFormProvider
                                                              ) extends PostcodeLookupController with Retrievals {

  override protected def form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, index).right.map(
        viewModel =>
          post(CompanyDirectorAddressPostCodeLookupId(index), viewModel, mode)
      )
  }

  private def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Either[Future[Result], PostcodeLookupViewModel] = {
    DirectorDetailsId(index).retrieve.right.map {
      director =>
        PostcodeLookupViewModel(
          routes.CompanyDirectorAddressPostCodeLookupController.onSubmit(mode, index),
          routes.DirectorAddressController.onPageLoad(mode, index),
          Message("companyDirectorAddressPostCodeLookup.title"),
          Message("companyDirectorAddressPostCodeLookup.heading"),
          Some(Message(director.fullName)),
          Message("companyDirectorAddressPostCodeLookup.body"),
          Message("companyDirectorAddressPostCodeLookup.enterPostcode"),
          Message("companyDirectorAddressPostCodeLookup.postcode"),
          Message("companyDirectorAddressPostCodeLookup.postcode.hint")
        )
    }
  }

}

/*

class CompanyDirectorAddressPostCodeLookupController @Inject()(
                                                                appConfig: FrontendAppConfig,
                                                                override val messagesApi: MessagesApi,
                                                                dataCacheConnector: DataCacheConnector,
                                                                addressLookupConnector: AddressLookupConnector,
                                                                @CompanyDirector navigator: Navigator,
                                                                authenticate: AuthAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: CompanyDirectorAddressPostCodeLookupFormProvider
                                                              ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", messageKey)
  }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        Future.successful(Ok(companyDirectorAddressPostCodeLookup(appConfig, form, mode, index, directorName)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(companyDirectorAddressPostCodeLookup(appConfig, formWithErrors, mode, index, directorName))),
          (value) =>
            addressLookupConnector.addressLookupByPostCode(value).flatMap {
              case Nil =>
                Future.successful(
                  BadRequest(
                    companyDirectorAddressPostCodeLookup(
                      appConfig,
                      formWithError("companyDirectorAddressPostCodeLookup.error.noResults"),
                      mode,
                      index,
                      directorName
                    )
                  )
                )


              case addresses =>

                dataCacheConnector
                  .save(
                    request.externalId,
                    CompanyDirectorAddressPostCodeLookupId(index),
                    addresses
                  )
                  .map(cacheMap =>
                    Redirect(
                      navigator.nextPage(CompanyDirectorAddressPostCodeLookupId(index), mode)(UserAnswers(cacheMap))
                    )
                  )
            }.recoverWith{
              case _ =>
                Future.successful(
                  BadRequest(
                    companyDirectorAddressPostCodeLookup(
                      appConfig,
                      formWithError("companyDirectorAddressPostCodeLookup.error.invalid"),
                      mode,
                      index,
                      directorName
                    )
                  )
                )
            }
        )
      }
  }
}
*/
