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
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import forms.register.company.DirectorPreviousAddressPostCodeLookupFormProvider
import identifiers.register.company.{DirectorDetailsId, DirectorPreviousAddressPostCodeLookupId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.register.company.directorPreviousAddressPostCodeLookup

import scala.concurrent.Future

class DirectorPreviousAddressPostCodeLookupController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        addressLookupConnector: AddressLookupConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: DirectorPreviousAddressPostCodeLookupFormProvider
                                      ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", messageKey)
  }

  def onPageLoad(mode: Mode, index:Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        Future.successful(Ok(directorPreviousAddressPostCodeLookup(appConfig, form, mode, index, directorName)))
      }
  }

  def onSubmit(mode: Mode, index:Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithErrors, mode, index, directorName))),
          (value) =>
            addressLookupConnector.addressLookupByPostCode(value).flatMap {
              case None =>
                Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithError("directorPreviousAddressPostCodeLookup.error.invalid"), mode, index, directorName)))
              case Some(Nil) =>
                Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithError("directorPreviousAddressPostCodeLookup.error.noResults"), mode, index, directorName)))
              case Some(addressRecords) =>
                val address = addressRecords.map(_.address)
                dataCacheConnector.save(request.externalId, DirectorPreviousAddressPostCodeLookupId(index), address).map(cacheMap =>
                Redirect(navigator.nextPage(DirectorPreviousAddressPostCodeLookupId(index), mode)(UserAnswers(cacheMap))))
            }
        )
      }
  }

  def retrieveDirectorName(index: Int)(block: String => Future[Result])(implicit request: DataRequest[AnyContent]):Future[Result] = {
    request.userAnswers.get(DirectorDetailsId(index)) match {
      case Some(value) =>
        block(value.fullName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
