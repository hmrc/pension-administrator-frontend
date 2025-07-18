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

import audit.{AuditService, EmailAuditEvent}
import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes._
import controllers.routes._
import identifiers.UpdateContactAddressId
import identifiers.register._
import models._
import models.enumeration.JourneyType
import models.register.DeclarationWorkingKnowledge
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{NoRLSCheck, Variations}
import utils.{Navigator, UserAnswers}
import views.html.register.variationDeclaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(appConfig: FrontendAppConfig,
                                               authenticate: AuthAction,
                                               @NoRLSCheck allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Variations navigator: Navigator,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               pensionAdministratorConnector: PensionAdministratorConnector,
                                               emailConnector: EmailConnector,
                                               auditService: AuditService,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: variationDeclaration)
                                              (implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val displayReturnLink =
          request
            .userAnswers
            .get(UpdateContactAddressId)
            .isEmpty

        val workingKnowledge =
          request
            .userAnswers
            .get(VariationWorkingKnowledgeId)
            .getOrElse(false)

        Future.successful(Ok(view(
          psaNameOpt = if (displayReturnLink) psaName() else None,
          isWorkingKnowldge = workingKnowledge,
          href = VariationDeclarationController.onClickAgree()
        )))
    }

  def onClickAgree(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData)
    .async { implicit request =>
      val workingKnowledge = request.userAnswers.get(VariationWorkingKnowledgeId).getOrElse(false)

      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
      dataCacheConnector.save(id = DeclarationId, value = true) flatMap { json =>
        val answers = UserAnswers(json).set(ExistingPSAId)(
          ExistingPSA(isExistingPSA = request.user.isExistingPSA,
            existingPSAId = request.user.existingPSAId)).asOpt
          .getOrElse(UserAnswers(json))
          .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.declarationWorkingKnowledge(workingKnowledge, isRegistrationToggleEnabled = false))
          .asOpt.getOrElse(UserAnswers(json))

        pensionAdministratorConnector.updatePsa(answers).flatMap { psaResponse =>
          sendEmail(psaId).map { _ =>
            if (psaResponse.status == 200) {
              Redirect(navigator.nextPage(DeclarationId, mode, UserAnswers(json)))
            } else {
              Redirect(YourActionWasNotProcessedController.onPageLoad())
            }
          }
        } recoverWith {
          case _: BadRequestException =>
            Future.successful(Redirect(SubmissionInvalidController.onPageLoad()))
          case _ =>
            Future.successful(Redirect(YourActionWasNotProcessedController.onPageLoad()))
        }
      }
    }

  private def sendEmail(psaId: String)
                       (implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[EmailStatus] =
    (psaEmail, psaName()) match {
      case (Some(email), Some(name)) =>
        emailConnector
          .sendEmail(
            emailAddress = email,
            templateName = appConfig.variationEmailTemplateId,
            templateParams = Map("psaName" -> name),
            psaId = PsaId(psaId),
            journeyType = JourneyType.VARIATION
          )
          .map { status =>
            auditService.sendEvent(EmailAuditEvent(psaId, "Variation", email))
            status
          }
      case _ => Future.successful(EmailNotSent)
    }
}
