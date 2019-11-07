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
import controllers.Retrievals
import controllers.actions._
import identifiers.register._
import identifiers.register.company.ContactDetailsId
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

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  dataCacheConnector: UserAnswersCacheConnector,
                                                  pensionsSchemeConnector: PensionsSchemeConnector,
                                                  knownFactsRetrieval: KnownFactsRetrieval,
                                                  enrolments: TaxEnrolmentsConnector,
                                                  emailConnector: EmailConnector
                                                 )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val cancelUrl = request.user.userType match {
        case UserType.Individual => individual.routes.WhatYouWillNeedController.onPageLoad()
        case UserType.Organisation => company.routes.WhatYouWillNeedController.onPageLoad()
      }
      Future.successful(Ok(
        declarationFitAndProper(appConfig, cancelUrl, controllers.register.routes.DeclarationFitAndProperController.onClickAgree())))
  }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, value = true).flatMap { cacheMap =>

        val answers = UserAnswers(cacheMap).set(ExistingPSAId)(ExistingPSA(
          request.user.isExistingPSA,
          request.user.existingPSAId
        )).asOpt.getOrElse(UserAnswers(cacheMap))

        (for {
          psaResponse <- pensionsSchemeConnector.registerPsa(answers)
          cacheMap <- dataCacheConnector.save(request.externalId, PsaSubscriptionResponseId, psaResponse)
          _ <- enrol(psaResponse.psaId)
          _ <- sendEmail(answers, psaResponse.psaId)
        } yield {
          Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode, UserAnswers(cacheMap)))
        }) recoverWith {
          case _: BadRequestException =>
            Future.successful(Redirect(controllers.register.routes.SubmissionInvalidController.onPageLoad()))
          case ex: Upstream4xxResponse if ex.message.contains("INVALID_BUSINESS_PARTNER") =>
            Future.successful(Redirect(controllers.register.routes.DuplicateRegistrationController.onPageLoad()))
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
      }
  }

  private def getEmail(answers: UserAnswers): Option[String] = {
    answers.get(RegistrationInfoId).flatMap { registrationInfo =>
      registrationInfo.legalStatus match {
        case Individual => answers.get(IndividualEmailId)
        case LimitedCompany => answers.get(ContactDetailsId).map(_.email)
        case Partnership => answers.get(PartnershipEmailId)
      }
    }
  }

  private def sendEmail(answers: UserAnswers, psaId: String)(implicit hc: HeaderCarrier): Future[EmailStatus] = {
    getEmail(answers) map { email =>
      emailConnector.sendEmail(email, appConfig.emailTemplateId, PsaId(psaId))
    } getOrElse Future.successful(EmailNotSent)
  }

  private def enrol(psaId: String)(implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[HttpResponse] = {
    knownFactsRetrieval.retrieve(psaId) map { knownFacts =>
      enrolments.enrol(psaId, knownFacts)
    } getOrElse Future.failed(KnownFactsRetrievalException())
  }

  case class KnownFactsRetrievalException() extends Exception {
    def apply(): Unit = Logger.error("Could not retrieve Known Facts")
  }

  case class PSANameNotFoundException() extends Exception("Could not retrieve PSA Name")

}
