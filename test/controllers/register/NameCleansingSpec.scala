/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register

import controllers.ControllerSpecBase
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Results.Redirect

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

class NameCleansingSpec extends ControllerSpecBase {
  private val nameWithInvalidCharacters = """abcdefgh~|ijklmnopqrstu!vw"xyz£01$%2^3()+-456@:;7#,.89 '&\/"""
  private val nameWithInvalidCharactersStrippedOut = """abcdefghijklmnopqrstuvwxyz0123456789 '&/"""

  private val controller = new NameCleansing{}

  private val fieldName = "companyName"

  private case class Data(name: String)

  private val form = Form(
    mapping(
      "name" -> text
    )(Data.apply)(Data.unapply)
  )

  private val dataBeforeBind = Map(
    "one" -> Seq(nameWithInvalidCharacters),
    "companyName" -> Seq(nameWithInvalidCharacters)
  )

  private val dataAfterBind = Map(
    "one" -> nameWithInvalidCharacters,
    "companyName" -> nameWithInvalidCharactersStrippedOut
  )

  "cleanse" must {
    "cleanse form data containing invalid characters in companyName field" in {
      controller.cleanse(dataBeforeBind, fieldName) mustBe dataAfterBind
    }
  }

  "cleanseAndBindOrRedirect" must {
    "redirect when there are no fields in body" in {
      val result = controller.cleanseAndBindOrRedirect(None, fieldName, form)
      result.isLeft mustBe true

      result.left.toOption.map {
        Await.result(_, 2.seconds) mustBe Redirect(controllers.routes.SessionExpiredController.onPageLoad)
      }
    }

    "cleanse and bind when there are fields in body" in {
      val result = controller.cleanseAndBindOrRedirect(Some(dataBeforeBind), fieldName, form)
      result.isRight mustBe true
      result.toOption.map( _.data mustBe dataAfterBind )
    }

  }
}
