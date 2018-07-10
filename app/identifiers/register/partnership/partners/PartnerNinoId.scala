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

package identifiers.register.partnership.partners

import identifiers.TypedIdentifier
import models.Nino
import play.api.libs.json.JsPath
import utils.checkyouranswers.NinoCYA

case class PartnerNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerNinoId.toString
}

object PartnerNinoId {
  override lazy val toString: String = "partnerNino"

  implicit val cya = NinoCYA("partnerNino.checkYourAnswersLabel", "partnerNino.checkYourAnswersLabel.nino")()

}
