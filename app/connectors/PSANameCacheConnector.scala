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

package connectors

import config.FrontendAppConfig
import identifiers.TypedIdentifier
import javax.inject.Inject
import play.api.libs.json.{Format, JsValue}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class PSANameCacheConnector @Inject()(
                                       config: FrontendAppConfig,
                                       http: WSClient,
                                       crypto: ApplicationCrypto
                                     ) extends MicroserviceCacheConnector(config, http, crypto) {

  override protected def url(id: String) = s"${config.pensionsSchemeUrl}/pensions-scheme/psa-name/$id"

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                               (implicit
                                                fmt: Format[A],
                                                ec: ExecutionContext,
                                                hc: HeaderCarrier
                                               ): Future[JsValue] = {

    val encryptedCacheId = crypto.QueryParameterCrypto.encrypt(PlainText(cacheId)).value
    modify(encryptedCacheId, _.set(id)(value))
  }

}
