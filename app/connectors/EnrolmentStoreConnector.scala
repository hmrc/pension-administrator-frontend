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

import javax.inject.Inject

import config.FrontendAppConfig
import models.register.KnownFacts
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Writes
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(val http: WSClient, config: FrontendAppConfig) {

  val url = config.enrolmentStoreUrl("HMRC-PSA-ORG")

  def enrol(knownFacts: KnownFacts)
           (implicit hc: HeaderCarrier, ec: ExecutionContext, w: Writes[KnownFacts]) = {
    http.url(url)
      .withHeaders(hc.headers: _*)
      .put(knownFacts)
      .map { response =>
        response.status match {
          case NO_CONTENT =>
            Future.successful(None)
          case _ =>
            Future.failed(new HttpException(response.body, response.status))
        }
      }
  }

}
