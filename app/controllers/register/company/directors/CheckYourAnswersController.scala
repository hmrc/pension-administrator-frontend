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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.company.directors.routes._
import controllers.{Retrievals, Variations}
import identifiers.register.company.directors._
import javax.inject.Inject
import models.Mode.checkMode
import models.{Mode, _}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.CompanyDirector
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{Enumerable, Navigator, SectionComplete}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            val allowAccess: AllowAccessActionProvider,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @CompanyDirector navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            sectionComplete: SectionComplete,
                                            override val cacheConnector: UserAnswersCacheConnector,
                                            implicit val countryOptions: CountryOptions
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController
  with Retrievals with Variations with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
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
            DirectorAddressId(index).row(Some(Link(DirectorAddressController.onPageLoad(checkMode(mode), index).url))) ++
            DirectorAddressYearsId(index).row(Some(Link(routes.DirectorAddressYearsController.onPageLoad(checkMode(mode), index).url))) ++
            DirectorPreviousAddressId(index).row(Some(Link(DirectorPreviousAddressController.onPageLoad(checkMode(mode), index).url))) ++
            DirectorEmailId(index).row(Some(Link(DirectorEmailController.onPageLoad(checkMode(mode), index).url))) ++
            DirectorPhoneId(index).row(Some(Link(DirectorPhoneController.onPageLoad(checkMode(mode), index).url)))
        ))

      Future.successful(Ok(check_your_answers(
        appConfig,
        answersSection,
        controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit(mode, index),
        psaName(),
        mode
      )))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setComplete(IsDirectorCompleteId(index), request.userAnswers) flatMap { _ =>
        saveChangeFlag(mode, CheckYourAnswersId).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
        }
      }
  }
}
