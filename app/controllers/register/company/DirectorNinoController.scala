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
import forms.register.company.DirectorNinoFormProvider
import identifiers.register.company.{DirectorDetailsId, DirectorNinoId}
import models.register.company.DirectorNino
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.company.directorNino

import scala.concurrent.Future

class DirectorNinoController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: DataCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: DirectorNinoFormProvider
                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits {

  private val form: Form[DirectorNino] = formProvider()

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        val redirectResult = request.userAnswers.get(DirectorNinoId(index)) match {
          case None =>
            Ok(directorNino(appConfig, form, mode, index, directorName))
          case Some(value) =>
            Ok(directorNino(appConfig, form.fill(value), mode, index, directorName))
        }
        Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorNino(appConfig, formWithErrors, mode, index, directorName))),
          (value) =>
            dataCacheConnector.save(request.externalId, DirectorNinoId(index), value).map(json =>
              Redirect(navigator.nextPage(DirectorNinoId(index), mode)(new UserAnswers(json))))
        )
      }
  }

  private def retrieveDirectorName(index:Int)(block: String => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(DirectorDetailsId(index)) match {
      case Some(value) =>
        block(value.fullName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
