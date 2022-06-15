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
import controllers.HasReferenceNumberController
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.company.routes._
import forms.HasReferenceNumberFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyTradingOverAYearId
import models.FeatureToggleName.PsaRegistration
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.annotations.{RegisterCompany, RegisterContactV2}
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyTradingOverAYearController @Inject()(override val appConfig: FrontendAppConfig,
                                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                                  @RegisterCompany override val navigator: Navigator,
                                                  @RegisterContactV2 val navigatorV2: Navigator,
                                                  authenticate: AuthAction,
                                                  allowAccess: AllowAccessActionProvider,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: HasReferenceNumberFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  val view: hasReferenceNumber,
                                                  featureToggleConnector: FeatureToggleConnector
                                                 )(implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = CompanyTradingOverAYearController.onSubmit(mode),
      title = Message("trading.title", Message("theCompany")),
      heading = Message("trading.title", companyName),
      mode = mode,
      hint = None,
      entityName = companyName
    )

  private def form(companyName: String)
                  (implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider("trading.error.required", companyName)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.right.map {
          companyName =>
            get(CompanyTradingOverAYearId, form(companyName), viewModel(mode, companyName))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        BusinessNameId.retrieve.right.map {
          companyName => {
            form(companyName).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(view(formWithErrors, viewModel(mode, companyName)))),
              value => {
                for {
                  isFeatureEnabled <- featureToggleConnector.get(PsaRegistration.asString).map(_.isEnabled)
                  newCache <- dataCacheConnector.save(request.externalId, CompanyTradingOverAYearId, value)
                } yield {
                  if (isFeatureEnabled) {
                    Redirect(navigatorV2.nextPage(CompanyTradingOverAYearId, mode, UserAnswers(newCache)))
                  } else {
                    Redirect(navigator.nextPage(CompanyTradingOverAYearId, mode, UserAnswers(newCache)))
                  }
                }
              }
            )
          }
        }
    }
}
