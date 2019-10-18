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

package controllers.register.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyReviewId
import javax.inject.Inject
import models.{Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.RegisterCompany
import views.html.register.company.companyReview

import scala.concurrent.{ExecutionContext, Future}

class CompanyReviewController @Inject()(appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        @RegisterCompany navigator: Navigator,
                                        authenticate: AuthAction,
                                        allowAccess: AllowAccessActionProvider,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction
                                       )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map { businessName =>
        val directors = request.userAnswers.allDirectorsAfterDelete(mode).map(_.name)
        Future.successful(Ok(companyReview(appConfig, businessName, directors)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CompanyReviewId, NormalMode, request.userAnswers))
  }
}
