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

import akka.actor.ActorSystem
import akka.pattern.Patterns.after
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import javax.inject._
import play.api.Logger
import uk.gov.hmrc.http.HttpException

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait RetryHelper  {


  val as: ActorSystem = ActorSystem()

  def retryOnFailure[T](f: () => Future[T])(implicit ec: ExecutionContext, config: FrontendAppConfig): Future[T] = {
    retryWithBackOff(1, config.retryWaitMs, f)
  }

  private def retryWithBackOff[T] (currentAttempt: Int,
                                   currentWait: Int,
                                   f: () => Future[T])(implicit ec: ExecutionContext, config: FrontendAppConfig): Future[T] = {

    f.apply().recoverWith {
      case e: HttpException =>
        if ( e.responseCode >= 500 && e.responseCode < 600 && currentAttempt < config.retryAttempts) {
          val wait = Math.ceil(currentWait * config.retryWaitFactor).toInt
          Logger.warn(s"Failure, retrying after $wait ms")
          after(wait.milliseconds, as.scheduler, ec, Future.successful(1)).flatMap { _ =>
            retryWithBackOff(currentAttempt + 1, wait.toInt, f)
          }
        } else {
          Future.failed(e)
        }
      case e =>
        Future.failed(e)
    }
  }
}