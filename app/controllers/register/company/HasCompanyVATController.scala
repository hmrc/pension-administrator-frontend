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

package controllers.register.company

import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.{HasReferenceNumberController, Retrievals}
import controllers.actions._
import controllers.register.company.routes.HasCompanyVATController
import forms.HasReferenceNumberFormProvider
import identifiers.register.{BusinessNameId, HasVATId}
import models.FeatureToggleName.PsaRegistration

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.{Navigator, UserAnswers}
import utils.annotations.RegisterCompany
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.{ExecutionContext, Future}

class HasCompanyVATController @Inject()(override val appConfig: FrontendAppConfig,
                                        override val dataCacheConnector: UserAnswersCacheConnector,
                                        @RegisterCompany override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: HasReferenceNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: hasReferenceNumber,
                                        featureToggleConnector: FeatureToggleConnector
                                       )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController with Retrievals {

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = HasCompanyVATController.onSubmit(mode),
      title = Message("hasVAT.heading", Message("theCompany")),
      heading = Message("hasVAT.heading", companyName),
      mode = mode,
      hint = None,
      entityName = companyName,
      returnLink = returnLink
    )

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] =
    formProvider("hasVAT.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
          val returnLinkCompanyName = if (featureEnabled) Some(companyTaskListUrl()) else None
          get(HasVATId, form(companyName), viewModel(mode, returnLinkCompanyName))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        form(companyName).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
              val returnLinkCompanyName = if (featureEnabled) Some(companyTaskListUrl()) else None
              Future.successful(BadRequest(view(formWithErrors, viewModel(mode, returnLinkCompanyName))))
            },
          value =>
            for {
              cacheMap <- dataCacheConnector.save(request.externalId, HasVATId, value)
              featureToggle <- featureToggleConnector.get(PsaRegistration.asString)
            } yield {
              val userSelectsNo = !value
              if (featureToggle.isEnabled && userSelectsNo) {
                Redirect(companydetails.routes.CheckYourAnswersController.onPageLoad())
              } else {
                Redirect(navigator.nextPage(HasVATId, mode, UserAnswers(cacheMap)))
              }
            }
        )
    }
}
