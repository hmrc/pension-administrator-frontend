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

import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions._
import controllers.register.EnterNumberController
import forms.register.company.CompanyRegistrationNumberFormProvider
import identifiers.register.company.CompanyRegistrationNumberId
import models.FeatureToggleName.PsaRegistration
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.annotations.{RegisterCompany, RegisterCompanyV2}
import utils.{Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.register.company.enterNumber

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationNumberController @Inject()(val appConfig: FrontendAppConfig,
                                                    val cacheConnector: UserAnswersCacheConnector,
                                                    @RegisterCompany val navigator: Navigator,
                                                    @RegisterCompanyV2 val navigatorV2: Navigator,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: CompanyRegistrationNumberFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: enterNumber,
                                                    featureToggleConnector: FeatureToggleConnector
                                                   )(implicit val executionContext: ExecutionContext) extends EnterNumberController with Retrievals {

  private val form = formProvider()

  private def viewModel(mode: Mode, returnLink: Option[String])(implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyRegistrationNumberController.onSubmit(mode),
      title = Message("companyRegistrationNumber.heading", Message("theCompany")),
      heading = Message("companyRegistrationNumber.heading", companyName),
      hint = Some(Message("companyRegistrationNumber.hint")),
      mode = mode,
      entityName = companyName,
      returnLink = returnLink
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
        val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
        get(CompanyRegistrationNumberId, form, viewModel(mode, returnLink))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          featureToggleConnector.get(PsaRegistration.asString).flatMap { feature =>
            val returnLink = if (feature.isEnabled) Some(companyTaskListUrl()) else None
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode, returnLink))))
          },
        value =>
          for {
            isFeatureEnabled <- featureToggleConnector.get(PsaRegistration.asString).map(_.isEnabled)
            newCache <- cacheConnector.save(request.externalId, CompanyRegistrationNumberId, value)
          } yield {
            if (isFeatureEnabled) {
              Redirect(navigatorV2.nextPage(CompanyRegistrationNumberId, mode, UserAnswers(newCache)))
            } else {
              Redirect(navigator.nextPage(CompanyRegistrationNumberId, mode, UserAnswers(newCache)))
            }
          }
      )
  }

}
