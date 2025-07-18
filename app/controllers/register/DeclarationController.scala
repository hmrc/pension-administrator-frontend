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

package controllers.register

import com.google.inject.Singleton
import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes._
import controllers.routes._
import identifiers.register._
import models.enumeration.JourneyType
import models.register.BusinessType._
import models.register.RegistrationStatus
import models.requests.DataRequest
import models.{ExistingPSA, Mode, NormalMode}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpException, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}
import views.html.register.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(
    appConfig: FrontendAppConfig,
    authenticate: AuthAction,
    allowAccess: AllowAccessActionProvider,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    allowDeclaration: AllowDeclarationActionProvider,
    @Register navigator: Navigator,
    dataCacheConnector: UserAnswersCacheConnector,
    pensionAdministratorConnector: PensionAdministratorConnector,
    knownFactsRetrieval: KnownFactsRetrieval,
    enrolments: TaxEnrolmentsConnector,
    emailConnector: EmailConnector,
    val controllerComponents: MessagesControllerComponents,
    val view: declaration
)(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private val logger = Logger(classOf[DeclarationController])

  def isPsaTypeCompany(userAnswers:UserAnswers): Boolean = {
    userAnswers.get(RegisterAsBusinessId).getOrElse(false)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen allowDeclaration(mode) andThen requireData).async {
      implicit request =>
        DeclarationWorkingKnowledgeId.retrieve.map {
          workingKnowledge =>
            Future.successful(Ok(view(workingKnowledge.hasWorkingKnowledge)))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen allowDeclaration(mode) andThen requireData).async {
      implicit request =>
        dataCacheConnector.save(
          id      = DeclarationId,
          value   = true
        ) flatMap { cacheMap =>
          val answers =
            UserAnswers(cacheMap)
              .set(ExistingPSAId)(
                ExistingPSA(
                  isExistingPSA = request.user.isExistingPSA,
                  existingPSAId = request.user.existingPSAId
                )
              )
              .asOpt
              .getOrElse(UserAnswers(cacheMap))

          (for {
            psaResponse <- pensionAdministratorConnector.registerPsa(answers)
            cacheMap    <- dataCacheConnector.save(PsaSubscriptionResponseId, psaResponse)
            _           <- enrol(psaResponse.psaId)
            emailStatus <- sendEmail(psaResponse.psaId)
          } yield {
            if (emailStatus == EmailNotSent) {
              Redirect(controllers.register.routes.InvalidEmailAddressController.onPageLoad(
                getBusinessType(UserAnswers(cacheMap)))
              )
            } else {
              Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
            }
          }) recoverWith handleOnSubmitErrors
        }
    }

  // Handle errors during the process
  private def handleOnSubmitErrors: PartialFunction[Throwable, Future[Result]] = {
    case _: BadRequestException =>
      Future.successful(Redirect(SubmissionInvalidController.onPageLoad()))
    case ex: UpstreamErrorResponse if ex.message.contains("INVALID_BUSINESS_PARTNER") =>
      Future.successful(Redirect(DuplicateRegistrationController.onPageLoad()))
    case ex: UpstreamErrorResponse if ex.message.contains("ACTIVE_PSAID") || ex.message.contains("INVALID_PSAID") =>
      Future.successful(Redirect(CannotRegisterAdministratorController.onPageLoad))
    case _: KnownFactsRetrievalException =>
      Future.successful(Redirect(SessionExpiredController.onPageLoad))
    case e: HttpException =>
      logger.error(s"Register PSA request responded with status code ${e.responseCode} and message: ${e.message}", e)
      Future.successful(Redirect(YourActionWasNotProcessedController.onPageLoad()))
    case e =>
      logger.error("Declaration error message: " + e.getMessage, e)
      Future.successful(Redirect(YourActionWasNotProcessedController.onPageLoad()))
  }

  private def sendEmail(psaId: String)
                       (implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[EmailStatus] =
    (psaEmail, psaName()) match {
      case (Some(email), Some(name)) =>
        emailConnector.sendEmail(
          emailAddress   = email,
          templateName   = emailTemplateName(request.userAnswers),
          templateParams = Map("psaName" -> name),
          psaId          = PsaId(psaId),
          journeyType    = JourneyType.PSA
        )
      case _ =>
        Future.successful(EmailNotSent)
    }

  private def emailTemplateName(userAnswers:UserAnswers):String ={
     if(isPsaTypeCompany(userAnswers)) {
       appConfig.companyEmailTemplateId
     } else {
       appConfig.emailTemplateId
     }
  }
  private def enrol(psaId: String)
                   (implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[HttpResponse] =
    knownFactsRetrieval.retrieve(psaId) map {
      knownFacts =>
        enrolments.enrol(psaId, knownFacts)
    } getOrElse Future.failed(KnownFactsRetrievalException())

  case class KnownFactsRetrievalException() extends Exception {
    def apply(): Unit = logger.error("Could not retrieve Known Facts")
  }

  case class PSANameNotFoundException() extends Exception("Could not retrieve PSA Name")

  private def getBusinessType(cacheMap: UserAnswers): RegistrationStatus = {
    if (cacheMap.get(RegisterAsBusinessId).getOrElse(false)) {
      cacheMap.get(BusinessTypeId).map {
        case LimitedCompany | UnlimitedCompany => RegistrationStatus.LimitedCompany
        case BusinessPartnership | LimitedPartnership | LimitedLiabilityPartnership => RegistrationStatus.Partnership
        case _ => throw new IllegalArgumentException("Invalid business type")
      }.getOrElse(throw new IllegalArgumentException("Business type not found"))
    } else {
      RegistrationStatus.Individual
    }
  }

}



