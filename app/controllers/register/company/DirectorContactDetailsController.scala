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
import controllers.Retrievals
import controllers.actions._
import forms.register.company.{ContactDetailsFormProvider, DirectorContactDetailsFormProvider}
import identifiers.register.company.DirectorContactDetailsId
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.register.company.directorContactDetails

import scala.concurrent.Future

class DirectorContactDetailsController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ContactDetailsFormProvider
                                      ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        val redirectResult = request.userAnswers.get(DirectorContactDetailsId(index)) match {
          case None => Ok(directorContactDetails(appConfig, form, mode, index, directorName))
          case Some(value) => Ok(directorContactDetails(appConfig, form.fill(value), mode, index, directorName))
        }
        Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorContactDetails(appConfig, formWithErrors, mode, index, directorName))),
          (value) =>
            dataCacheConnector.save(request.externalId, DirectorContactDetailsId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(DirectorContactDetailsId(index), mode)(new UserAnswers(cacheMap))))
        )
      }
  }
}
