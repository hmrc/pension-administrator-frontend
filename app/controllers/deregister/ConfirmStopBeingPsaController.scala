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

package controllers.deregister

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import connectors.{DeregistrationConnector, MinimalPsaConnector, TaxEnrolmentsConnector}
import controllers.actions.{AllowAccessForNonSuspendedUsersAction, AuthAction}
import forms.deregister.ConfirmStopBeingPsaFormProvider
import javax.inject.Inject
import models.MinimalPSA
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.deregister.confirmStopBeingPsa
import utils.PSAConstants.PSA_ACTIVE_RELATIONSHIP_EXISTS

import scala.concurrent.{ExecutionContext, Future}

class ConfirmStopBeingPsaController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               auth: AuthAction,
                                               formProvider: ConfirmStopBeingPsaFormProvider,
                                               minimalPsaConnector: MinimalPsaConnector,
                                               deregistrationConnector: DeregistrationConnector,
                                               enrolments: TaxEnrolmentsConnector,
                                               allowAccess: AllowAccessForNonSuspendedUsersAction,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: confirmStopBeingPsa
                                             )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (auth andThen allowAccess).async {
    implicit request =>
      request.user.alreadyEnrolledPsaId.map { _ =>
        deregistrationConnector.canDeRegister.flatMap {
          case deregistration if deregistration.canDeregister =>
            minimalPsaConnector.getMinimalPsaDetails().map { minimalDetails =>
              getPsaName(minimalDetails) match {
                case Some(psaName) => Ok(view(form, psaName))
                case _ => Redirect(controllers.routes.SessionExpiredController.onPageLoad)
              }
            }
          case deregistration if deregistration.isOtherPsaAttached =>
            Future.successful(Redirect(controllers.deregister.routes.CannotDeregisterController.onPageLoad()))
          case _ =>
            Future.successful(Redirect(controllers.deregister.routes.MustInviteOthersController.onPageLoad()))
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
      )
  }

  def onSubmit(): Action[AnyContent] = auth.async {
    implicit request =>
      request.user.alreadyEnrolledPsaId.map { psaId =>
        minimalPsaConnector.getMinimalPsaDetails().flatMap {
          minimalDetails =>
            getPsaName(minimalDetails) match {
              case Some(psaName) =>
                form.bindFromRequest().fold(
                  (formWithErrors: Form[Boolean]) =>
                    Future.successful(BadRequest(view(formWithErrors, psaName))),
                  value => {
                    if (value) {
                      for {
                        response <- deregistrationConnector.stopBeingPSA
                        result <- if (response.status == FORBIDDEN && response.body.contains(PSA_ACTIVE_RELATIONSHIP_EXISTS)) {
                          Future.successful(Redirect(controllers.deregister.routes.CannotDeregisterController.onPageLoad()))
                        } else {
                          for {
                            _ <- enrolments.deEnrol(request.user.groupIdentifier, psaId, request.externalId)
                            _ <- dataCacheConnector.removeAll
                          } yield {
                            Redirect(controllers.deregister.routes.SuccessfulDeregistrationController.onPageLoad())
                          }
                        }
                      } yield result
                    } else {
                      Future.successful(Redirect(Call("GET", appConfig.schemesOverviewUrl)))
                    }
                  }
                )
              case _ =>
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
            }
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
      )

  }

  private def getPsaName(minimalDetails: MinimalPSA): Option[String] = {
    (minimalDetails.individualDetails, minimalDetails.organisationName) match {
      case (Some(individual), None) => Some(individual.fullName)
      case (None, Some(org)) => Some(s"$org")
      case _ => None
    }
  }

}
