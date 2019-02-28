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

package controllers.vary

import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import forms.vary.DeclarationFitAndProperFormProvider
import identifiers.register._
import identifiers.register.company.{BusinessDetailsId, ContactDetailsId}
import identifiers.register.individual.{IndividualContactDetailsId, IndividualDetailsId}
import identifiers.register.partnership.PartnershipContactDetailsId
import javax.inject.Inject
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.UserType.UserType
import models._
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class DeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  formProvider: DeclarationFitAndProperFormProvider,
                                                  dataCacheConnector: UserAnswersCacheConnector,
                                                  pensionsSchemeConnector: PensionsSchemeConnector,
                                                  knownFactsRetrieval: KnownFactsRetrieval,
                                                  enrolments: TaxEnrolmentsConnector,
                                                  emailConnector: EmailConnector
                                                 )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  private def getPsaName(userType: UserType)(implicit request: DataRequest[AnyContent]): String = {
    val optionPsaName = userType match {
      case UserType.Individual =>
        request.userAnswers.get(IndividualDetailsId).map(_.fullName)
      case UserType.Organisation =>
        request.userAnswers.get(BusinessDetailsId).map(_.companyName)
      case _ => None
    }
    optionPsaName.getOrElse("")
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      request.user.userType match {
        case UserType.Individual =>
          Future.successful(Ok(
            views.html.vary.declarationFitAndProper(appConfig, form, getPsaName(UserType.Individual))))
        case UserType.Organisation =>
          Future.successful(Ok(
            views.html.vary.declarationFitAndProper(appConfig, form, getPsaName(UserType.Organisation))))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          request.user.userType match {
            case UserType.Individual =>
              Future.successful(BadRequest(
                views.html.vary.declarationFitAndProper(appConfig, errors, getPsaName(UserType.Individual))))

            case UserType.Organisation =>
              Future.successful(BadRequest(
                views.html.vary.declarationFitAndProper(appConfig, errors, getPsaName(UserType.Organisation))))
          },
        success =>
          dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).flatMap { cacheMap =>

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
              case _: InvalidPayloadException =>
                Future.successful(Redirect(controllers.register.routes.SubmissionInvalidController.onPageLoad()))
              case _: InvalidBusinessPartnerException =>
                Future.successful(Redirect(controllers.register.routes.DuplicateRegistrationController.onPageLoad()))
              case _ =>
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
            }
          }
      )
  }

  private def getEmail(answers: UserAnswers): Option[String] = {
    answers.get(RegistrationInfoId).flatMap { registrationInfo =>
      val id = registrationInfo.legalStatus match {
        case Individual => IndividualContactDetailsId
        case LimitedCompany => ContactDetailsId
        case Partnership => PartnershipContactDetailsId
      }
      answers.get(id).map { contactDetails =>
        contactDetails.email
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
