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

package controllers.register.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.partnership.{PartnershipDetailsId, PartnershipReviewId}
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Partnership
import views.html.register.partnership.partnershipReview

import scala.concurrent.Future

class PartnershipReviewController @Inject()(appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            @Partnership navigator: Navigator,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnershipDetailsId.retrieve.right.map { details =>
        val partners = request.userAnswers.allPartnersAfterDelete.map(_.name)
        Future.successful(Ok(partnershipReview(appConfig, details.name, partners)))
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(PartnershipReviewId, NormalMode, request.userAnswers))
  }
}
