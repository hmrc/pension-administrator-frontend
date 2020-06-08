/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.MinimalPsaConnector
import connectors.DeregistrationConnector
import connectors.TaxEnrolmentsConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.AllowAccessForNonSuspendedUsersAction
import controllers.actions.AuthAction
import forms.deregister.ConfirmStopBeingPsaFormProvider
import javax.inject.Inject
import models.MinimalPSA
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.deregister.confirmStopBeingPsa

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ConfirmStopBeingPsaController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               auth: AuthAction,
                                               override val messagesApi: MessagesApi,
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
      request.user.alreadyEnrolledPsaId.map { psaId =>
        deregistrationConnector.canDeRegister(psaId).flatMap {
          case true =>
            minimalPsaConnector.getMinimalPsaDetails(psaId).map { minimalDetails =>
              getPsaName(minimalDetails) match {
                case Some(psaName) => Ok(view(form, psaName))
                case _ => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
              }
            }
          case false =>
            Future.successful(Redirect(controllers.deregister.routes.CannotDeregisterController.onPageLoad()))
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      )
  }

  def onSubmit: Action[AnyContent] = auth.async {
    implicit request =>
      request.user.alreadyEnrolledPsaId.map { psaId =>
        val userId = request.user.userId
        minimalPsaConnector.getMinimalPsaDetails(psaId).flatMap {
          minimalDetails =>
            getPsaName(minimalDetails) match {
              case Some(psaName) =>
                form.bindFromRequest().fold(
                  (formWithErrors: Form[Boolean]) =>
                    Future.successful(BadRequest(view(formWithErrors, psaName))),
                  value => {
                    if (value) {
                      for {
                        _ <- deregistrationConnector.stopBeingPSA(psaId)
                        _ <- enrolments.deEnrol(userId, psaId, request.externalId)
                        _ <- dataCacheConnector.removeAll(request.externalId)
                      } yield {
                        Redirect(controllers.deregister.routes.SuccessfulDeregistrationController.onPageLoad())
                      }
                    } else {
                      Future.successful(Redirect(Call("GET", appConfig.schemesOverviewUrl)))
                    }
                  }
                )
              case _ =>
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
            }
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
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
