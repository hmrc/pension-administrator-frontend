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

package utils

import com.google.inject.{ImplementedBy, Inject}
import connectors.DataCacheConnector
import identifiers.TypedIdentifier
import models.register.company.directors.DirectorDetails
import models.requests.DataRequest
import play.api.libs.json.JsResultException
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent._

@ImplementedBy(classOf[SectionCompleteImpl])
trait SectionComplete {

  def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                 (implicit request: DataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers]

  def directorsWithFlag(
                         directors: Seq[DirectorDetails],
                         flags: Seq[Boolean],
                         withFlag: Seq[(DirectorDetails, Boolean)] = Seq.empty
                       ): Seq[(DirectorDetails, Boolean)]

}

class SectionCompleteImpl @Inject()(dataCacheConnector: DataCacheConnector) extends SectionComplete {

  override def setComplete(id: TypedIdentifier[Boolean], userAnswers: UserAnswers)
                          (implicit request: DataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {

    userAnswers.set(id)(true).fold(
      invalid => Future.failed(JsResultException(invalid)),
      valid => Future.successful(valid)
    )

    dataCacheConnector.save(request.externalId, id, true) map UserAnswers

  }

  override def directorsWithFlag(
                                 directors: Seq[DirectorDetails],
                                 flags: Seq[Boolean],
                                 withFlag: Seq[(DirectorDetails, Boolean)] = Seq.empty
                               ): Seq[(DirectorDetails, Boolean)] = {

    if(directors.isEmpty){
      withFlag
    } else if (flags.isEmpty) {
      val addWithFlag: (DirectorDetails, Boolean) = (directors.head, false)
      directorsWithFlag(directors.tail, Seq.empty, withFlag :+ addWithFlag)
    } else {
      val addWithFlag: (DirectorDetails, Boolean) = (directors.head, flags.head)
      directorsWithFlag(directors.tail, flags.tail, withFlag :+ addWithFlag)
    }

  }


}