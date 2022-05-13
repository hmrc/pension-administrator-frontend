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

package connectors

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import connectors.cache.FeatureToggleConnector
import models.FeatureToggleName.FromIvToPdvForAdmin
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class PersonalDetailsValidationConnectorImpl @Inject()(http: HttpClient, frontendAppConfig: FrontendAppConfig, featureToggleConnector: FeatureToggleConnector)
  extends PersonalDetailsValidationConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[PersonalDetailsValidationConnectorImpl])

  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    val jsonData = Json.obj(
      "origin" -> "PODS",
      "completionURL" -> completionURL,
      "failureURL" -> failureURL,
      "confidenceLevel" -> 200
    )

    val url = frontendAppConfig.ivRegisterOrganisationAsIndividualUrl

    http.POST[JsObject, HttpResponse](url, jsonData).map { response =>
      response.status match {
        case CREATED =>
          (response.json \ "link").validate[String] match {
            case JsSuccess(value, _) => value
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          handleErrorResponse("POST", url)(response)
      }
    } andThen {
      logExceptions("Unable to start registration of organisation as individual via IV")
    }
  }

  override def retrieveNino(journeyId: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]] = {


    featureToggleConnector.get(FromIvToPdvForAdmin.asString).map { toggle =>
      toggle.isEnabled
    }.flatMap {
      case true =>
        val url = s"${frontendAppConfig.personalDetailsValidation}/personal-details-validation/$journeyId"

        http.GET[HttpResponse](url).map {
          case response if response.status equals OK =>
            (response.json \ "personalDetails" \ "nino").asOpt[Nino]
          case response =>
            logger.debug(s"Call to retrieve Nino failed with status ${response.status} and response body ${response.body}")
            None
        }
      case false =>
        val url = s"${frontendAppConfig.identityVerification}/identity-verification/journey/$journeyId"

        http.GET[HttpResponse](url).map {
          case response if response.status equals OK =>
            (response.json \ "nino").asOpt[Nino]
          case response =>
            logger.debug(s"Call to retrieve Nino from IV failed with status ${response.status} and response body ${response.body}")
            None
        }
    }
  } andThen {
    logExceptions("Unable to retrieve Nino")
  }

  private def logExceptions[T](msg: String): PartialFunction[Try[T], Unit] = {
    case Failure(t: Throwable) => logger.error(msg, t)
  }
}

@ImplementedBy(classOf[PersonalDetailsValidationConnectorImpl])
trait PersonalDetailsValidationConnector {
  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String]

  def retrieveNino(journeyId: String)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]]
}
