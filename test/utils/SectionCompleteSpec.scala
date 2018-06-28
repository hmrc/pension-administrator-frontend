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

import connectors.DataCacheConnector
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{PSAUser, UserType}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SectionCompleteSpec extends WordSpec with MustMatchers with OptionValues with MockitoSugar{

  private val dummyId=new TypedIdentifier[Boolean]{
    override def toString="DummyId"
  }
  private val existingId=new TypedIdentifier[String]{
    override def toString="existingId"
  }

  implicit val request: DataRequest[AnyContent] = DataRequest(
    request = FakeRequest("", ""),
    externalId = "cacheId",
    user = PSAUser(UserType.Individual, None, false, None),
    userAnswers = UserAnswers()
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val dataCacheConnector = mock[DataCacheConnector]

  val userAnswers = UserAnswers(Json.obj(existingId.toString -> "testId"))

  "set Complete" must {
    "set the isComplete identifier to true" in {

      val expected = userAnswers.set(dummyId)(true).asOpt.value
      reset(dataCacheConnector)
      when(dataCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expected.json))

      val result=new SectionCompleteImpl(dataCacheConnector).setComplete(dummyId, UserAnswers())

      result.map{answers=>
        answers mustBe expected
      }
    }

    "save userAnswers to mongo" in {
      val expected = userAnswers.set(dummyId)(true).asOpt.value

      reset(dataCacheConnector)
      when(dataCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expected.json))

      val result=new SectionCompleteImpl(dataCacheConnector).setComplete(dummyId, userAnswers)
      result.map{
        _ => verify(dataCacheConnector).save(any(), eqTo(dummyId), eqTo(true))(any(), any(), any())
          succeed
      }
    }


  }

  "answersWithFlag" must {

    "return a collection of answers with false flags" when {
      "an empty collection of flags is passed" in {

      }
    }

    "return a collection of answers with flags" when {
      "a collection of answers and flags are passed" in {

      }
    }

    "return an empty collection" when {
      "an empty collection of answers is passed" in {

      }
    }

  }

}
