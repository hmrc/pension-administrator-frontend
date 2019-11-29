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

package utils

import com.google.inject.{ImplementedBy, Inject}
import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import models.requests.DataRequest
import play.api.libs.json.JsResultException
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent._

@ImplementedBy(classOf[SectionCompleteImpl])
trait SectionComplete {

  def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                 (implicit request: DataRequest[AnyContent], executionContext: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers]

}

class SectionCompleteImpl @Inject()(dataCacheConnector: UserAnswersCacheConnector) extends SectionComplete {

  override def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                          (implicit request: DataRequest[AnyContent], executionContext: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {

    userAnswers.set(id)(true).fold(
      invalid => Future.failed(JsResultException(invalid)),
      valid => Future.successful(valid)
    )

    dataCacheConnector.save(request.externalId, id, true) map UserAnswers

  }


}