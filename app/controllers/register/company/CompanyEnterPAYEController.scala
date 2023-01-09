/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.EnterPAYEController
import forms.EnterPAYEFormProvider
import identifiers.register.EnterPAYEId
import models.FeatureToggleName.PsaRegistration
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.annotations.{RegisterCompany, RegisterCompanyV2}
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterPAYE

import scala.concurrent.{ExecutionContext, Future}

class CompanyEnterPAYEController @Inject()(val appConfig: FrontendAppConfig,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           @RegisterCompany val navigator: Navigator,
                                           @RegisterCompanyV2 val navigatorV2: Navigator,
                                           authenticate: AuthAction,
                                           allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EnterPAYEFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: enterPAYE,
                                           featureToggleConnector: FeatureToggleConnector
                                          )(implicit val executionContext: ExecutionContext) extends EnterPAYEController with Retrievals {

  protected def form(companyName: String)
                    (implicit request: DataRequest[AnyContent]): Form[String] = formProvider(companyName)

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.company.routes.CompanyEnterPAYEController.onSubmit(mode),
      title = Message("enterPAYE.heading", Message("theCompany")),
      heading = Message("enterPAYE.heading", companyName),
      mode = mode,
      hint = Some(Message("enterPAYE.hint")),
      entityName = companyName,
      returnLink = returnLink
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
        val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
        get(EnterPAYEId, form(companyName), viewModel(mode, returnLink))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form(companyName).bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          featureToggleConnector.enabled(PsaRegistration).flatMap { featureEnabled =>
            val returnLink = if (featureEnabled) Some(companyTaskListUrl()) else None
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode, returnLink))))
          },
        value =>
          for {
            isFeatureEnabled <- featureToggleConnector.get(PsaRegistration.asString).map(_.isEnabled)
            newCache <- cacheConnector.save(request.externalId, EnterPAYEId, value)
          } yield {
            if (isFeatureEnabled) {
              Redirect(navigatorV2.nextPage(EnterPAYEId, mode, UserAnswers(newCache)))
            } else {
              Redirect(navigator.nextPage(EnterPAYEId, mode, UserAnswers(newCache)))
            }
          }
      )
  }
}
