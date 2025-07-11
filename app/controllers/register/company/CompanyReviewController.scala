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

package controllers.register.company

import controllers.Retrievals
import controllers.actions._
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyReviewId
import models.{Mode, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Navigator
import utils.annotations.RegisterCompany
import views.html.register.company.companyReview

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyReviewController @Inject()(
                                         @RegisterCompany navigator: Navigator,
                                         authenticate: AuthAction,
                                         allowAccess: AllowAccessActionProvider,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: companyReview
                                       )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.map { businessName =>
        val directors = request.userAnswers.allDirectorsAfterDelete(mode).map(_.name)
        Future.successful(Ok(view(businessName, directors)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CompanyReviewId, NormalMode, request.userAnswers))
  }
}
