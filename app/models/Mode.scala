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

package models

import play.api.mvc.{JavascriptLiteral, PathBindable}
import utils.WithName

sealed trait Mode

case object NormalMode extends WithName("") with Mode

case object CheckMode extends WithName("change") with Mode

case object UpdateMode extends WithName("changing") with Mode

case object CheckUpdateMode extends WithName("update") with Mode

object Mode {

  case class UnknownModeException() extends Exception

  implicit def modePathBindable(implicit stringBinder: PathBindable[String]): PathBindable[Mode] = new PathBindable[Mode] {

    val modes = Seq(NormalMode, CheckMode, UpdateMode, CheckUpdateMode)

    override def bind(key: String, value: String): Either[String, Mode] = {
      stringBinder.bind(key, value) match {
        case Right(CheckMode.toString) => Right(CheckMode)
        case Right(UpdateMode.toString) => Right(UpdateMode)
        case Right(CheckUpdateMode.toString) => Right(CheckUpdateMode)
        case _ => Left("Mode binding failed")
      }
    }

    override def unbind(key: String, value: Mode): String = {
      val modeValue = modes.find(_ == value).map(_.toString).getOrElse(throw UnknownModeException())
      stringBinder.unbind(key, modeValue)
    }
  }

  implicit val jsLiteral: JavascriptLiteral[Mode] = new JavascriptLiteral[Mode] {
    override def to(value: Mode): String = value match {
      case NormalMode => "NormalMode"
      case CheckMode => "CheckMode"
      case UpdateMode => "UpdateMode"
      case CheckUpdateMode => "CheckUpdateMode"
    }
  }

  def checkMode(mode: Mode): Mode = mode match  {
    case NormalMode => CheckMode
    case UpdateMode => CheckUpdateMode
    case _ => throw UnknownModeException()
  }

  def journeyMode(mode: Mode): Mode = mode match  {
    case CheckMode => NormalMode
    case CheckUpdateMode => UpdateMode
    case _ => throw UnknownModeException()
  }



}
