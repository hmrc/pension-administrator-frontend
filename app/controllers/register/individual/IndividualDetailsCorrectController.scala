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

package controllers.register.individual

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.individual.IndividualDetailsCorrectFormProvider
import identifiers.register.individual.IndividualDetailsCorrectId
import models.{Mode, TolerantAddress, TolerantIndividual}
import play.api.mvc.{Action, AnyContent}
import utils.{Navigator, UserAnswers}
import views.html.register.individual.individualDetailsCorrect

import scala.concurrent.Future

class IndividualDetailsCorrectController @Inject() (
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: IndividualDetailsCorrectFormProvider
                                                   ) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndividualDetailsCorrectId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val individual = TolerantIndividual(
        Some("John"),
        Some("T"),
        Some("Doe")
      )

      val address = TolerantAddress(
        Some("Building Name"),
        Some("1 Main Street"),
        Some("Some Village"),
        Some("Some Town"),
        Some("GB"),
        Some("ZZ1 1ZZ")
      )

      Ok(individualDetailsCorrect(appConfig, preparedForm, mode, individual, address))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val individual = TolerantIndividual(
        Some("John"),
        Some("T"),
        Some("Doe")
      )

      val address = TolerantAddress(
        Some("Building Name"),
        Some("1 Main Street"),
        Some("Some Village"),
        Some("Some Town"),
        Some("GB"),
        Some("ZZ1 1ZZ")
      )

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(individualDetailsCorrect(appConfig, formWithErrors, mode, individual, address))),
        (value) =>
          dataCacheConnector.save(request.externalId, IndividualDetailsCorrectId, value).map(cacheMap =>
            Redirect(navigator.nextPage(IndividualDetailsCorrectId, mode)(UserAnswers(cacheMap))))
      )
  }

}
