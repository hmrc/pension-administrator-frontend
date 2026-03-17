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

package controllers.register.individual

import com.google.inject.{Inject, Singleton}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.individual.routes.*
import identifiers.register.individual.*
import models.Mode
import models.Mode.checkMode
import models.admin.ukResidencyToggle
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.{AuthWithIV, Individual}
import utils.checkyouranswers.Ops.*
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import utils.navigators.IndividualNavigatorV2
import utils.{Enumerable, Navigator}
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(
                                            @AuthWithIV authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            dataCompletion: DataCompletion,
                                            @Individual navigator: Navigator,
                                            individualNavigatorV2: IndividualNavigatorV2,
                                            override val messagesApi: MessagesApi,
                                            implicit val countryOptions: CountryOptions,
                                            val controllerComponents: MessagesControllerComponents,
                                            featureFlagService: FeatureFlagService,
                                            view: check_your_answers
                                          )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).flatMap { ukResidency =>
        val ua = request.userAnswers
        (ua.get(IndividualDetailsId), ua.get(IndividualAddressId)) match {
          case (Some(_), Some(_)) =>
            Future.successful(loadCyaPage(mode, ukResidency.isEnabled))
          case _ =>
            Future.successful(Redirect(controllers.register.routes.RegisterAsBusinessController.onPageLoad()))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      featureFlagService.get(ukResidencyToggle).flatMap { ukResidency =>
        val isDataComplete: Boolean = if(ukResidency.isEnabled) {
          dataCompletion.isIndividualUKComplete(request.userAnswers, mode)
        } else {
          dataCompletion.isIndividualComplete(request.userAnswers, mode)
        }
        (isDataComplete, ukResidency.isEnabled) match {
          case (true, true) => Future.successful(Redirect(individualNavigatorV2.nextPage(CheckYourAnswersId, mode, request.userAnswers)))
          case (true, false) => Future.successful(Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers)))
          case _ => Future.successful(loadCyaPage(mode, ukResidency.isEnabled))
        }

      }
  }

  private def loadCyaPage(mode: Mode, ukResidency: Boolean)(implicit request: DataRequest[AnyContent]): Result = {
      val correctContactAddress = if (ukResidency) {
        IndividualUKContactAddressId.row(Some(Link(IndividualContactAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url)))
      } else {
        IndividualContactAddressId.row(Some(Link(IndividualContactAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url)))
      }
      val individualDetails = AnswerSection(None, Seq(
        IndividualDetailsId.row(None),
        IndividualDateOfBirthId.row(Some(Link(IndividualDateOfBirthController.onPageLoad(checkMode(mode)).url))),
        IndividualAddressId.row(None),
        IndividualSameContactAddressId.row(Some(Link(IndividualSameContactAddressController.onPageLoad(checkMode(mode)).url))),
        correctContactAddress,
        IndividualAddressYearsId.row(Some(Link(IndividualAddressYearsController.onPageLoad(checkMode(mode)).url))),
        IndividualPreviousAddressId.row(Some(Link(
          controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode)).url))),
        IndividualEmailId.row(Some(Link(IndividualEmailController.onPageLoad(checkMode(mode)).url))),
        IndividualPhoneId.row(Some(Link(IndividualPhoneController.onPageLoad(checkMode(mode)).url)))
      ).flatten)
      val sections = Seq(individualDetails)
      val dataComplete = if(ukResidency) {
        dataCompletion.isIndividualUKComplete(request.userAnswers, mode)
      } else {
        dataCompletion.isIndividualComplete(request.userAnswers, mode)
      }
      Ok(view(sections, postUrl, None, mode, dataComplete))
    }
}
