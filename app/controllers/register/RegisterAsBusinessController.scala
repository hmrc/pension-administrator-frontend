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

package controllers.register

import audit.{AuditService, PSAStartEvent}
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRetrievalAction}
import forms.register.RegisterAsBusinessFormProvider
import identifiers.register.{BusinessTypeId, RegisterAsBusinessId, RegistrationInfoId}
import models.FeatureToggleName.PsaRegistration
import models.RegistrationCustomerType.UK
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoPDV
import views.html.register.registerAsBusiness

import scala.concurrent.{ExecutionContext, Future}

class RegisterAsBusinessController @Inject()(appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             @AuthWithNoPDV authenticate: AuthAction,
                                             allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             cache: UserAnswersCacheConnector,
                                             featureToggleConnector: FeatureToggleConnector,
                                             auditService: AuditService,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: registerAsBusiness
                                            )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers match {
        case None => form
        case Some(ua) => ua.get(RegisterAsBusinessId).fold(form)(form.fill)
      }
      Ok(view(preparedForm))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          Future.successful(BadRequest(view(errors))),
        isBusiness => {
          for {
            _ <- cache.save(request.externalId, RegisterAsBusinessId, isBusiness)
            _ = PSAStartEvent.sendEvent(auditService)
            isFeatureEnabled <- featureToggleConnector.get(PsaRegistration.asString).map(_.isEnabled)
          } yield {
            (isBusiness, isFeatureEnabled) match {
              case (false, _) => Redirect(individual.routes.WhatYouWillNeedController.onPageLoad())
              case (true, false) => Redirect(routes.WhatYouWillNeedController.onPageLoad(NormalMode))
              case (true, true) =>
                val businessType = request.userAnswers.flatMap(_.get(BusinessTypeId))
                val customerType = request.userAnswers.flatMap(_.get(RegistrationInfoId).map(_.customerType))
                (businessType, customerType) match {
                  case (Some(_), Some(UK)) => Redirect(routes.ContinueWithRegistrationController.onPageLoad())
                  case _ => Redirect(routes.WhatYouWillNeedController.onPageLoad(NormalMode))
                }
            }
          }
        }
      )
  }
}
