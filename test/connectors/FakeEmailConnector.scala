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

package connectors

import org.scalatest.Matchers
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait FakeEmailConnector extends EmailConnector with Matchers {
  var count: Int = 0
  override def sendEmail(emailAddress: String, templateName: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus] = {
    println("\n\n\n called this\n\n")
    count += 1
    Future(EmailSent)
  }

  def reset() = count = 0

  def calledOnce = count == 1

  def calledNever = count == 0

  def verify(result: Boolean) = {
    println("result"+result)
    println("count : "+count)
    result shouldBe true
  }
}

object FakeEmailConnector extends FakeEmailConnector


