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

package controllers.register

import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register._
import identifiers.register.company.CompanyEmailId
import identifiers.register.individual.IndividualEmailId
import identifiers.register.partnership.PartnershipEmailId
import javax.inject.Inject
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.DataRequest
import models.{ExistingPSA, Mode, NormalMode, UserType}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}
import views.html.register.declarationFitAndProper
import controllers.register.routes.DeclarationFitAndProperController
import controllers.register.routes.{DuplicateRegistrationController, SubmissionInvalidController}

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  dataCacheConnector: UserAnswersCacheConnector
                                                 )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      Future.successful(Ok(
        declarationFitAndProper(appConfig)))
  }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, value = true).map { cacheMap =>
        Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode, UserAnswers(cacheMap)))
      }
  }


}
