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

package controllers

import com.google.inject.{ImplementedBy, Inject, Singleton}
import controllers.JourneyType.JourneyType
import models.requests.DataRequest
import play.api.mvc.Result
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals => HmrcRetrievals}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[JourneyImpl])
trait Journey {

  def withJourneyType(f: (JourneyType) => Future[Result])(implicit request: DataRequest[_], ec: ExecutionContext): Future[Result]

}

@Singleton
class JourneyImpl @Inject()(override val authConnector: AuthConnector)  extends Journey with AuthorisedFunctions with BaseController {

  def withJourneyType(f: (JourneyType) => Future[Result])(implicit request: DataRequest[_], ec: ExecutionContext): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised().retrieve(HmrcRetrievals.affinityGroup) {
      case Some(Individual) => f(JourneyType.Individual)
      case Some(Organisation) => f(JourneyType.Company)
      case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    }
  }

}

object JourneyType extends Enumeration {
  type JourneyType = Value
  val Individual, Company = Value
}
