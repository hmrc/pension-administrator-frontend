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

package utils.navigators

import base.SpecBase
import controllers.register.company.routes
import identifiers.register.company._
import models._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import utils.UserAnswers

class RegisterCompanyNavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new RegisterCompanyNavigator()

  val emptyAnswers = new UserAnswers(Json.obj())
  val testIndex = 0

  "Navigator" when {

    "in Normal mode" must {

      "if user answers no to business address, go to the 'update address' page" in {

        navigator.nextPage(CompanyAddressId, NormalMode)(emptyAnswers) mustBe
          routes.CompanyUpdateDetailsController.onPageLoad()
      }

      "if user answers yes to business address, go to the 'update address' page" in {


        val answers = UserAnswers(Json.obj())
          .set(CompanyAddressId)(TolerantAddress(Some("100"),
              Some("SuttonStreet"),
              Some("Wokingham"),
              Some("Surrey"),
              Some("NE39 1HX"),
              Some("GB")))
          .asOpt.value
        


        navigator.nextPage(CompanyAddressId, NormalMode)(answers) mustBe
          routes.WhatYouWillNeedController.onPageLoad()
      }
    }
  }
}
