/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.FakeRegistrationConnector
import identifiers.register.RegistrationInfoId
import models._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class RegistrationServiceSpec extends AsyncFlatSpec with Matchers with OptionValues {

  import RegistrationServiceSpec._


  val service = new RegistrationServiceImpl(userAnswersCacheConnector, registrationConnector)

  "RegistrationService" should "do the registration and return future unit" in{

   service.registerWithNoIdIndividual(extId, individual, address, dob).map{
     result=>
       userAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
       result shouldBe registrationInfo
   }

  }

  it should "throw the exception if first name is missing" in {

    a[MandatoryIndividualDetailsMissing] shouldBe thrownBy {
      service.registerWithNoIdIndividual(extId, individual.copy(firstName = None), address, dob)
    }

  }

  it should "throw the exception if last name is missing" in {

    a[MandatoryIndividualDetailsMissing] shouldBe thrownBy {
      service.registerWithNoIdIndividual(extId, individual.copy(lastName = None), address, dob)
    }

  }

}

object RegistrationServiceSpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def userAnswersCacheConnector: FakeUserAnswersCacheConnector = FakeUserAnswersCacheConnector

  private val registrationConnector = new FakeRegistrationConnector {

    override def registerWithNoIdIndividual(
        firstName: String, lastName: String, address: Address, dateOfBirth: LocalDate)(
        implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[RegistrationInfo] = Future.successful(registrationInfo)
  }

  private val extId: String = "ext-id"

  private val fName: String = "John"
  private val lName: String = "fName"

  private val individual = TolerantIndividual(
    Some(fName),
    None,
    Some(lName)
  )

  private val address = Address(
    "Building Name",
    "1 Main Street",
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    "GB"
  )

  private val dob: LocalDate = LocalDate.now()

  val sapNumber = "test-sap-number"

  val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.Individual,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )
}
