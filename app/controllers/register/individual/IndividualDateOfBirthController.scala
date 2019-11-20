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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.Retrievals
import forms.register.individual.IndividualDateOfBirthFormProvider
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import identifiers.register.AreYouInUKId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import utils.annotations.Individual
import views.html.register.individual.individualDateOfBirth

import scala.concurrent.{ExecutionContext, Future}

class IndividualDateOfBirthController @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 dataCacheConnector: UserAnswersCacheConnector,
                                                 @Individual navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: IndividualDateOfBirthFormProvider,
                                                 registrationService: RegistrationService
                                               )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndividualDateOfBirthId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(individualDateOfBirth(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(individualDateOfBirth(appConfig, formWithErrors, mode))),
        value =>
          (AreYouInUKId and IndividualDetailsId and IndividualAddressId).retrieve.right.map {
            case false ~ individual ~ address =>
              registrationService.registerWithNoIdIndividual(request.externalId, individual, address.toAddress,
                new LocalDate(value.getYear, value.getMonthValue, value.getDayOfMonth)).flatMap { _ =>
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
