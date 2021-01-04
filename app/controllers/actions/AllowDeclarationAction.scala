/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import identifiers.register.RegistrationInfoId
import models.Mode
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.OptionalDataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.UserAnswers
import utils.dataCompletion.DataCompletion

import scala.concurrent.{ExecutionContext, Future}

class AllowDeclarationAction(mode: Mode, dataCompletion: DataCompletion)
                            (implicit val executionContext: ExecutionContext) extends ActionFilter[OptionalDataRequest] {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    val userAnswers = request.userAnswers.getOrElse(UserAnswers())

    val isComplete = userAnswers.get(RegistrationInfoId).map(_.legalStatus) match {
      case Some(Individual) =>
        dataCompletion.isIndividualComplete(userAnswers, mode)
      case Some(LimitedCompany) =>
        dataCompletion.isCompanyComplete(userAnswers, mode)
      case Some(Partnership) =>
        dataCompletion.isPartnershipComplete(userAnswers, mode)
      case _ =>
        true
    }

    if (isComplete && dataCompletion.isAdviserComplete(userAnswers, mode)) {
      Future(None)
    } else {
      Future.successful(Some(Redirect(controllers.register.routes.RegisterAsBusinessController.onPageLoad())))
    }
  }
}

class AllowDeclarationActionProviderImpl @Inject()(dataCompletion: DataCompletion)
                                                  (implicit executionContext: ExecutionContext) extends AllowDeclarationActionProvider {
  def apply(mode: Mode): AllowDeclarationAction = {
    new AllowDeclarationAction(mode, dataCompletion)
  }
}

trait AllowDeclarationActionProvider {
  def apply(mode: Mode): AllowDeclarationAction
}
