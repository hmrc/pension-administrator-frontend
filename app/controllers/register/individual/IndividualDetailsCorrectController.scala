/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.{RegistrationConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.individual.IndividualDetailsCorrectFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsCorrectId, IndividualDetailsId}
import javax.inject.Inject
import models.{Mode, UserType}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Individual
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import views.html.register.individual.individualDetailsCorrect

import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsCorrectController @Inject()(
                                                    @Individual navigator: Navigator,
                                                    appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    dataCacheConnector: UserAnswersCacheConnector,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: IndividualDetailsCorrectFormProvider,
                                                    registrationConnector: RegistrationConnector,
                                                    countryOptions: CountryOptions
                                                  )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDetailsCorrectId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val existingIndividualDetailsId = request.userAnswers.get(IndividualDetailsId)
      val existingIndividualAddressId = request.userAnswers.get(IndividualAddressId)
      val existingRegistrationInfo = request.userAnswers.get(RegistrationInfoId)

      (existingIndividualDetailsId, existingIndividualAddressId, existingRegistrationInfo) match {
        case (Some(individual), Some(address), Some(_)) =>
          Future.successful(Ok(individualDetailsCorrect(appConfig, preparedForm, mode, individual, address, countryOptions)))
        case _ =>
          request.user.nino match {
            case Some(nino) =>
              for {
                registration <- registrationConnector.registerWithIdIndividual(nino)
                _ <- dataCacheConnector.save(request.externalId, IndividualDetailsId, registration.response.individual)
                _ <- dataCacheConnector.save(request.externalId, IndividualAddressId, registration.response.address)
                _ <- dataCacheConnector.save(request.externalId, RegistrationInfoId, registration.info)
              } yield {
                if(request.user.userType==UserType.Organisation){
                  Logger.warn("Organisation user after successful manual IV uplift")
                }
                Ok(individualDetailsCorrect(appConfig, preparedForm, mode, registration.response.individual, registration.response.address, countryOptions))
              }
            case _ =>
              Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
          }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          (IndividualDetailsId and IndividualAddressId).retrieve.right.map {
            case individual ~ address =>
              Future.successful(BadRequest(individualDetailsCorrect(appConfig, formWithErrors, mode, individual, address, countryOptions)))
          }
        },
        value =>
          dataCacheConnector.save(request.externalId, IndividualDetailsCorrectId, value).map(cacheMap =>
            Redirect(navigator.nextPage(IndividualDetailsCorrectId, mode, UserAnswers(cacheMap))))
      )
  }

}
