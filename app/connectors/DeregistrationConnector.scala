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

import java.util.UUID.randomUUID

import com.google.inject.{ImplementedBy, Inject}
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import javax.inject.Singleton
import models._
import models.registrationnoid.RegistrationNoIdIndividualRequest
import org.joda.time.LocalDate
import play.Logger
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.Toggles.IsManualIVEnabled
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[DeregistrationConnectorImpl])
trait DeregistrationConnector {
  def stopBeingPSA(utr: String)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue]
}

@Singleton
class DeregistrationConnectorImpl @Inject()(http: HttpClient,
                                          config: FrontendAppConfig,
                                          fs: FeatureSwitchManagementService
                                         ) extends DeregistrationConnector {

  private val readsSapNumber: Reads[String] = (JsPath \ "sapNumber").read[String]

  override def stopBeingPSA(utr: String)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {

    val url = config.registerWithIdOrganisationUrl

    http.POST(url, "") map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithIdOrganisation is 200 OK")

      Json.parse(response.body)

    } andThen {
      case Failure(ex: NotFoundException) =>
        Logger.warn("Organisation not found with registerWithIdOrganisation", ex)
        ex
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithIdOrganisation", ex)
        ex
    }
  }
}
