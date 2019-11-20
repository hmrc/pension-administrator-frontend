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

package controllers.register.individual

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.individual.{CheckYourAnswersId, IndividualDetailsId, IndividualEmailId, IndividualPhoneId}
import models.{CheckMode, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Individual
import utils.checkyouranswers.Ops._
import utils.{CheckYourAnswersFactory, Enumerable, Navigator}
import viewmodels.{AnswerSection, Link, Message}
import views.html.check_your_answers

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            allowAccess: AllowAccessActionProvider,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @Individual navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            checkYourAnswersFactory: CheckYourAnswersFactory,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: check_your_answers
                                          )(implicit val ec: ExecutionContext) extends FrontendBaseController with Retrievals with I18nSupport with Enumerable.Implicits {

  import CheckYourAnswersController._

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      val helper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)

      val message = IndividualDetailsId.retrieve.right.map { details =>
        Message("individualAddressYears.title", details.fullName).resolve
      }.right.getOrElse(Message("cya.label.address.years").resolve)

      val section = AnswerSection(
        None,
        Seq(
          helper.individualDetails.toSeq,
          helper.individualDateOfBirth.toSeq,
          helper.individualAddress.toSeq,
          helper.individualSameContactAddress.toSeq,
          helper.individualContactAddress.toSeq,
          helper.individualAddressYears(message).toSeq,
          helper.individualPreviousAddress.toSeq,
          helper.individualEmail.toSeq,
          helper.individualPhone.toSeq
        ).flatten
      )
      val sections = Seq(section)
      Ok(view(sections, postUrl, None, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
  }
}

object CheckYourAnswersController {
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
}
