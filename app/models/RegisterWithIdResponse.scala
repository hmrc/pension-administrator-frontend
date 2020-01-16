/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

abstract class RegisterWithIdResponse(address: TolerantAddress)

case class OrganizationRegisterWithIdResponse(organisation: Organisation, address: TolerantAddress) extends RegisterWithIdResponse(address)

case class IndividualRegisterWithIdResponse(individual: TolerantIndividual, address: TolerantAddress) extends RegisterWithIdResponse(address)

case class OrganizationRegistration(response: OrganizationRegisterWithIdResponse, info: RegistrationInfo)

case class IndividualRegistration(response: IndividualRegisterWithIdResponse, info: RegistrationInfo)

object RegisterWithIdResponse {

  implicit lazy val readsOrganizationRegisterWithIdResponse: Reads[OrganizationRegisterWithIdResponse] =
    ((JsPath \ "organisation").read[Organisation] ~ (JsPath \ "address").read[TolerantAddress]).apply(OrganizationRegisterWithIdResponse)

  implicit lazy val writesOrganizationRegisterWithIdResponse: Writes[OrganizationRegisterWithIdResponse] =
    Writes[OrganizationRegisterWithIdResponse] { response =>
      Json.obj(
        "address" -> response.address,
        "organisation" -> response.organisation
      )
    }

  implicit lazy val formatsIndividualRegisterWithIdResponse: Format[IndividualRegisterWithIdResponse] = (
    (JsPath \ "individual").format[TolerantIndividual] and
      (JsPath \ "address").format[TolerantAddress]
    ) (IndividualRegisterWithIdResponse.apply, unlift(IndividualRegisterWithIdResponse.unapply))

}
