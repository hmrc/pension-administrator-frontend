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

package identifiers.register

import identifiers._
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId, AdviserAddressPostCodeLookupId, AdviserDetailsId}
import models.register.DeclarationWorkingKnowledge
import play.api.libs.json.JsResult
import utils.UserAnswers

case object DeclarationWorkingKnowledgeId extends TypedIdentifier[DeclarationWorkingKnowledge] {
  self =>
  override def toString: String = "declarationWorkingKnowledge"

  override def cleanup(value: Option[DeclarationWorkingKnowledge], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(DeclarationWorkingKnowledge.WorkingKnowledge) =>
        userAnswers.remove(AdviserDetailsId)
          .flatMap(_.remove(AdviserAddressPostCodeLookupId))
          .flatMap(_.remove(AdviserAddressListId))
          .flatMap(_.remove(AdviserAddressId))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}
