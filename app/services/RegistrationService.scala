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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import identifiers.register.RegistrationInfoId
import models.{Address, RegistrationInfo, TolerantIndividual}
import java.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class MandatoryIndividualDetailsMissing(msg: String) extends Exception

@ImplementedBy(classOf[RegistrationServiceImpl])
trait RegistrationService {
  def registerWithNoIdIndividual(extId: String,
      individual: TolerantIndividual,
      address: Address,
      dob: LocalDate
  )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[RegistrationInfo]
}

class RegistrationServiceImpl @Inject()(
    dataCacheConnector: UserAnswersCacheConnector,
    registrationConnector: RegistrationConnector) extends RegistrationService {

  def registerWithNoIdIndividual(extId: String,
      individual: TolerantIndividual,
      address: Address,
      dob: LocalDate
  )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[RegistrationInfo] = for {
    registrationInfo <- registrationConnector.registerWithNoIdIndividual(
      individual.firstName.getOrElse(error("First name missing")),
      individual.lastName.getOrElse(error("Last name missing")),
      address,
      dob)
    _ <- dataCacheConnector.save(extId, RegistrationInfoId, registrationInfo)
  } yield {
    registrationInfo
  }

  private def error(msg: String) = throw MandatoryIndividualDetailsMissing(msg)

}
