/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.TolerantAddress
import play.api.http.Status.*
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.Logging
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnectorImpl @Inject()(httpV2Client: HttpClientV2, config: FrontendAppConfig)
  extends AddressLookupConnector with Logging {

  override def addressLookupByPostCode(postcode: String)
                                      (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[TolerantAddress]] = {
    implicit val reads: Reads[Seq[TolerantAddress]] = TolerantAddress.postCodeLookupReads

    httpV2Client
      .post(url"${config.addressLookUp}/lookup")
      .setHeader(("X-Hmrc-Origin", "PODS"))
      .withBody(Json.obj("postcode" -> postcode))
      .execute[HttpResponse]
      .flatMap {
        case response if response.status.equals(OK) =>
          Future.successful(
            response
              .json
              .as[Seq[TolerantAddress]]
              .filterNot(
                a => a.addressLine1.isEmpty && a.addressLine2.isEmpty && a.addressLine3.isEmpty && a.addressLine4.isEmpty
              )
          )
        case response =>
          val message = s"Address Lookup failed with status ${response.status} Response body :${response.body}"
          Future.failed(new HttpException(message, response.status))
      }
      .recoverWith {
        case t: Throwable =>
          logger.error("Exception in AddressLookup", t)
          Future.failed(t)
      }

  }
}

@ImplementedBy(classOf[AddressLookupConnectorImpl])
trait AddressLookupConnector {
  def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[TolerantAddress]]
}
