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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.cache.{FeatureToggleConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.register.company.directors.routes._
import controllers.{Retrievals, Variations}
import identifiers.register.company.directors._
import models.FeatureToggleName.PsaRegistration
import models.Mode.checkMode
import models.requests.DataRequest
import models._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.CompanyDirector
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.{Enumerable, Navigator}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            val allowAccess: AllowAccessActionProvider,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            dataCompletion: DataCompletion,
                                            @CompanyDirector navigator: Navigator,
                                            override val cacheConnector: UserAnswersCacheConnector,
                                            implicit val countryOptions: CountryOptions,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: check_your_answers,
                                            featureToggleConnector: FeatureToggleConnector
                                          )(implicit val executionContext: ExecutionContext) extends FrontendBaseController
  with Retrievals with Variations with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(mode, index) { _ =>
        loadCyaPage(index, mode)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val isDirectorComplete = dataCompletion.isDirectorComplete(request.userAnswers, index)
      if (isDirectorComplete) {
        saveChangeFlag(mode, CheckYourAnswersId).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
        }
      } else {
        loadCyaPage(index, mode)
      }
  }

  private def loadCyaPage(index: Int, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val answersSection = Seq(
      AnswerSection(None,
        DirectorNameId(index).row(Some(Link(routes.DirectorNameController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorDOBId(index).row(Some(Link(routes.DirectorDOBController.onPageLoad(checkMode(mode), index).url))) ++
          HasDirectorNINOId(index).row(Some(Link(routes.HasDirectorNINOController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorEnterNINOId(index).row(Some(Link(routes.DirectorEnterNINOController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorNoNINOReasonId(index).row(Some(Link(routes.DirectorNoNINOReasonController.onPageLoad(checkMode(mode), index).url))) ++
          HasDirectorUTRId(index).row(Some(Link(HasDirectorUTRController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorEnterUTRId(index).row(Some(Link(DirectorEnterUTRController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorNoUTRReasonId(index).row(Some(Link(DirectorNoUTRReasonController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorAddressId(index).row(Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorAddressYearsId(index).row(Some(Link(routes.DirectorAddressYearsController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorPreviousAddressId(index).row(Some(Link(DirectorPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorEmailId(index).row(Some(Link(DirectorEmailController.onPageLoad(checkMode(mode), index).url))) ++
          DirectorPhoneId(index).row(Some(Link(DirectorPhoneController.onPageLoad(checkMode(mode), index).url)))
      ))

    featureToggleConnector.enabled(PsaRegistration).ifA(
      ifTrue =
        Ok(view(
          answersSection,
          controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit(mode, index),
          psaName(),
          mode,
          dataCompletion.isDirectorComplete(request.userAnswers, index),
          returnLink = taskListReturnLinkUrl()
        )),
      ifFalse =
        Ok(view(
          answersSection,
          controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit(mode, index),
          None,
          mode,
          dataCompletion.isDirectorComplete(request.userAnswers, index)
        ))
    )
  }
}
