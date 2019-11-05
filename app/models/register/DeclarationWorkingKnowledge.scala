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

package models.register

import utils.{Enumerable, InputOption, WithName}

sealed trait DeclarationWorkingKnowledge {
  def hasWorkingKnowledge:Boolean
}

object DeclarationWorkingKnowledge extends Enumerable.Implicits {

  case object WorkingKnowledge extends WithName("workingKnowledge") with DeclarationWorkingKnowledge {
    def hasWorkingKnowledge:Boolean = true
  }

  case object Adviser extends WithName("adviser") with DeclarationWorkingKnowledge {
    def hasWorkingKnowledge:Boolean = false
  }

  val values: Seq[DeclarationWorkingKnowledge] = Seq(
    WorkingKnowledge, Adviser
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"declarationWorkingKnowledge.${value.toString}")
  }

  implicit val enumerable: Enumerable[DeclarationWorkingKnowledge] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def declarationWorkingKnowledge(workingKnowledge: Boolean): DeclarationWorkingKnowledge =
    if(workingKnowledge) WorkingKnowledge else Adviser
}
