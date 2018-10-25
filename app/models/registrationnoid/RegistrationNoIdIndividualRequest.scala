package models.registrationnoid

import models.Address
import org.joda.time.LocalDate
import play.api.libs.json._

case class RegistrationNoIdIndividualRequest(firstName: String, lastName: String, dateOfBirth: LocalDate, address: Address)

object RegistrationNoIdIndividualRequest {

  implicit val formats: Format[RegistrationNoIdIndividualRequest] = Json.format[RegistrationNoIdIndividualRequest]
}
