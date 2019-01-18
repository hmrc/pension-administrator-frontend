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

package controllers.register

import controllers.ControllerSpecBase
import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import play.api.mvc.Results.Redirect

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class NameCleansingSpec extends ControllerSpecBase {
  private val nameWithInvalidCharacters = "Nik's Pensions Company (UK)"
  private val nameWithInvalidCharactersStrippedOut = "Nik's Pensions Company UK"

  private val controller = new NameCleansing{}

  private val fieldName = "companyName"

  private val businessDetailsFormModel = BusinessDetailsFormModel(
    companyNameMaxLength = 105,
    companyNameRequiredMsg = "partnershipName.error.required",
    companyNameLengthMsg = "partnershipName.error.length",
    companyNameInvalidMsg = "partnershipName.error.invalid",
    utrMaxLength = 10,
    utrRequiredMsg = None,
    utrLengthMsg = "businessDetails.error.utr.length",
    utrInvalidMsg = "businessDetails.error.utr.invalid"
  )

  private val formProvider = new BusinessDetailsFormProvider(isUK = false)
  private val form = formProvider(businessDetailsFormModel)

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
      val actualRedirect = Await.result(result.left.toOption.get, Duration.Inf)
      actualRedirect mustBe Redirect(controllers.routes.SessionExpiredController.onPageLoad())
    }

    "cleanse and bind when there are fields in body" in {
      val result = controller.cleanseAndBindOrRedirect(Some(dataBeforeBind), fieldName, form)
      result.isRight mustBe true
      val boundData = result.right.toOption.get.data
      boundData mustBe boundData
    }

  }
}
