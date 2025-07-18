/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.AddressFormProvider
import forms.register.individual.IndividualDetailsCorrectFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsCorrectId, IndividualDetailsId}
import models.RegistrationIdType.Nino
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{AuthWithIV, Individual}
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, Navigator, UserAnswers}
import views.html.register.individual.individualDetailsCorrect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsCorrectController @Inject()(@Individual navigator: Navigator,
                                                   dataCacheConnector: UserAnswersCacheConnector,
                                                   @AuthWithIV
                                                   authenticate: AuthAction,
                                                   allowAccess: AllowAccessActionProvider,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: IndividualDetailsCorrectFormProvider,
                                                   addressFormProvider: AddressFormProvider,
                                                   registrationConnector: RegistrationConnector,
                                                   countryOptions: CountryOptions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   val view: individualDetailsCorrect,
                                                   addressHelper: AddressHelper
                                                  )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDetailsCorrectId) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.user.nino match {
        case None => Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad))
        case Some(nino) =>
          (request.userAnswers.get(IndividualDetailsId), request.userAnswers.get(IndividualAddressId), request.userAnswers.get(RegistrationInfoId)) match {
            case (Some(individual), Some(address), Some(info)) if info.idType.contains(Nino) && info.idNumber.contains(nino.value) =>
              Future.successful(Ok(view(preparedForm, mode, individual, address, countryOptions)))
            case _ =>
              for {
                registration <- registrationConnector.registerWithIdIndividual(nino)
                _ <- dataCacheConnector.save(IndividualDetailsId, registration.response.individual)
                _ <- dataCacheConnector.save(IndividualAddressId, registration.response.address)
                _ <- dataCacheConnector.save(RegistrationInfoId, registration.info)
              } yield {
                Ok(view(preparedForm, mode, registration.response.individual, registration.response.address, countryOptions))
              }
          }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) => {
          (IndividualDetailsId and IndividualAddressId).retrieve.map {
            case individual ~ address =>
              Future.successful(BadRequest(view(formWithErrors, mode, individual, address, countryOptions)))
          }
        },
        value => {
          val invalidAddressFields = IndividualAddressId.retrieve.map { address =>
            val addressFields = addressHelper.mapAddressFields(address)
            val addressForm: Form[Address] = addressFormProvider()
            val bind = addressForm.bind(addressFields)
            bind.fold(
              _ => true,
              _ => false
            )
          }
          invalidAddressFields.map { invalidFields =>
            if (invalidFields) {
              Future.successful(Redirect(routes.AddressController.onPageLoad()))
            } else {
              dataCacheConnector.save(IndividualDetailsCorrectId, value).map(cacheMap =>
                Redirect(navigator.nextPage(IndividualDetailsCorrectId, mode, UserAnswers(cacheMap)))
              )
            }
          }
        }
      )
  }
}
