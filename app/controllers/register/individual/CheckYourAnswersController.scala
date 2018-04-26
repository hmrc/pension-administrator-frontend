/*
 * Copyright 2018 HM Revenue & Customs
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
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.individual.{CheckYourAnswersId, IndividualDetailsId}
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Individual
import utils.{CheckYourAnswersFactory, Navigator}
import viewmodels.{AnswerSection, Message}
import views.html.check_your_answers

@Singleton
class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @Individual navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            checkYourAnswersFactory: CheckYourAnswersFactory
                                          ) extends FrontendController with Retrievals with I18nSupport {

  import CheckYourAnswersController._

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val helper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)

      val message = IndividualDetailsId.retrieve.right.map { details =>
        Message("individualAddressYears.title", details.fullName).resolve
      }.right.getOrElse(Message("cya.label.address.years").resolve)

        val section = AnswerSection(
          None,
          Seq(
            helper.individualDetails,
            helper.individualAddress,
            helper.individualAddressYears(message),
            helper.individualPreviousAddress,
            helper.individualEmailAddress,
            helper.individualPhoneNumber
          ).flatten
        )

        val sections = Seq(section)

        Ok(check_your_answers(appConfig, sections, Some("site.secondaryHeader"), postUrl))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, mode)(request.userAnswers))
  }

}

object CheckYourAnswersController {

  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()

}
