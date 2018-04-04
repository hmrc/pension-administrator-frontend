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

import config.FrontendAppConfig
import connectors.{DataCacheConnector, RegistrationConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.individual.IndividualDetailsCorrectFormProvider
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsCorrectId, IndividualDetailsId}
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
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
                                                     formProvider: IndividualDetailsCorrectFormProvider,
                                                     registrationConnector: RegistrationConnector
                                                   ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDetailsCorrectId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      (IndividualDetailsId and IndividualAddressId).retrieve match {
        case Right(individual ~ address) =>
          Future.successful(Ok(individualDetailsCorrect(appConfig, preparedForm, mode, individual, address)))
        case _ =>
          for {
            response <- registrationConnector.registerWithIdIndividual()
            _ <- dataCacheConnector.save(request.externalId, IndividualDetailsId, response.individual)
            _ <- dataCacheConnector.save(request.externalId, IndividualAddressId, response.address)
          } yield {
            Ok(individualDetailsCorrect(appConfig, preparedForm, mode, response.individual, response.address))
          }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          (IndividualDetailsId and IndividualAddressId).retrieve.right.map {
            case individual ~ address =>
              Future.successful(BadRequest(individualDetailsCorrect(appConfig, formWithErrors, mode, individual, address)))
          }
        },
        (value) =>
          dataCacheConnector.save(request.externalId, IndividualDetailsCorrectId, value).map(cacheMap =>
            Redirect(navigator.nextPage(IndividualDetailsCorrectId, mode)(UserAnswers(cacheMap))))
      )
  }

}
