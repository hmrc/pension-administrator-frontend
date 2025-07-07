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

package controllers.register

import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRetrievalAction}
import forms.register.YesNoFormProvider
import identifiers.register.{BusinessTypeId, RegistrationInfoId}
import models.NormalMode
import models.RegistrationCustomerType.UK
import models.register.BusinessType.{LimitedCompany, UnlimitedCompany}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.AuthWithNoIV
import views.html.register.continueWithRegistration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContinueWithRegistrationController @Inject()(
                                                    val controllerComponents: MessagesControllerComponents,
                                                    @AuthWithNoIV authenticate: AuthAction,
                                                    getData: DataRetrievalAction,
                                                    continueWithRegistration: continueWithRegistration,
                                                    yesNoFormProvider: YesNoFormProvider,
                                                    cache: UserAnswersCacheConnector
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  val form: Form[Boolean] = yesNoFormProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData).async { implicit request =>
    val finishedBusinessMatching = request.userAnswers.flatMap(_.get(RegistrationInfoId)).isDefined
    if (finishedBusinessMatching) {
      Future.successful(Ok(continueWithRegistration(form)))
    } else {
      Future.successful(Redirect(routes.RegisterAsBusinessController.onPageLoad()))
    }
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData).async { implicit request =>
    form.bindFromRequest().fold(
      errors => Future.successful(BadRequest(continueWithRegistration(errors))),
      continueRegistration => if (continueRegistration) {
        val businessType = request.userAnswers.flatMap(_.get(BusinessTypeId))
        val customerType = request.userAnswers.flatMap(_.get(RegistrationInfoId).map(_.customerType))
        val result = (businessType, customerType) match {
          case (Some(LimitedCompany) | Some(UnlimitedCompany), Some(UK)) =>
            Redirect(company.routes.CompanyRegistrationTaskListController.onPageLoad())
          case (Some(_), Some(UK)) =>
            Redirect(administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad())
          case _ => Redirect(routes.WhatYouWillNeedController.onPageLoad(NormalMode))
        }
        Future.successful(result)
      } else {
        cache.removeAll.map(_ =>
          Redirect(routes.WhatYouWillNeedController.onPageLoad(NormalMode))
        )
      }
    )
  }
}
