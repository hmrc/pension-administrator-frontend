/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.individual.IndividualDateOfBirthFormProvider
import identifiers.register.AreYouInUKId
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Individual
import utils.{Navigator, UserAnswers}
import views.html.register.individual.individualDateOfBirth

import scala.concurrent.{ExecutionContext, Future}

class IndividualDateOfBirthController @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 dataCacheConnector: UserAnswersCacheConnector,
                                                 @Individual navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: IndividualDateOfBirthFormProvider,
                                                 registrationService: RegistrationService,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: individualDateOfBirth
                                                 )(implicit val executionContext: ExecutionContext
                                                ) extends FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndividualDateOfBirthId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          (AreYouInUKId and IndividualDetailsId and IndividualAddressId).retrieve.map {
            case false ~ individual ~ address =>
              registrationService.registerWithNoIdIndividual(request.externalId, individual, address.toAddress.get,
                value).flatMap { _ =>
                saveAndRedirect(mode, value)
              }
            case true ~ _ ~ _ =>
              saveAndRedirect(mode, value)
          }

      )
  }

  private def saveAndRedirect(mode: Mode, value: java.time.LocalDate)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    dataCacheConnector.save(request.externalId, IndividualDateOfBirthId, value).map(cacheMap =>
      Redirect(navigator.nextPage(IndividualDateOfBirthId, mode, UserAnswers(cacheMap))))
  }
}
