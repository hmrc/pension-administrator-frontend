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

      "Go to the correct page from user's answer" must {
        "if user answers no to business address, go to the 'update address' page" in {

          navigator.nextPage(ConfirmCompanyAddressId, NormalMode)(emptyAnswers) mustBe
            routes.CompanyUpdateDetailsController.onPageLoad()
        }

        "if user answers yes to business address, go to the 'what you need' page" in {

          val answers = UserAnswers(Json.obj())
            .set(ConfirmCompanyAddressId)(TolerantAddress(Some("100"),
              Some("SuttonStreet"),
              Some("Wokingham"),
              Some("Surrey"),
              Some("NE39 1HX"),
              Some("GB")))
            .asOpt.value

          navigator.nextPage(ConfirmCompanyAddressId, NormalMode)(answers) mustBe
            routes.WhatYouWillNeedController.onPageLoad()
        }
      }

      "Go to the Company Details page from the What you will need page" in {
        navigator.nextPage(WhatYouWillNeedId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyDetailsController.onPageLoad(NormalMode)
      }

      "Go to the 'CRN' page from 'Company Details' page" in {
        navigator.nextPage(CompanyDetailsId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
      }

      "Go to the 'Company Address' page from the Company Registration Number page" in {
        navigator.nextPage(CompanyRegistrationNumberId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyAddressController.onPageLoad()
      }

      "Go to the 'Company Address Years' page from the 'Company Address' page" in {
        navigator.nextPage(CompanyAddressId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyAddressYearsController.onPageLoad(NormalMode)
      }

      "Go to the correct page from user's answers on Company Address Years" must {

        "Go to the 'Company Contact' page if answers is 'Over a Year'" in {

          val answers = UserAnswers(Json.obj())
            .set(CompanyAddressYearsId)(AddressYears.OverAYear)
            .asOpt.value

          navigator.nextPage(CompanyAddressYearsId, NormalMode)(answers) mustBe
            routes.ContactDetailsController.onPageLoad(NormalMode)
        }

        "Go to the 'Previous Address Postcode lookup' page if answers is 'Under a Year'" in {

          val answers = UserAnswers(Json.obj())
            .set(CompanyAddressYearsId)(AddressYears.UnderAYear)
            .asOpt.value

          navigator.nextPage(CompanyAddressYearsId, NormalMode)(answers) mustBe
            routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
        }
      }

      "Go to the 'Previous Address' page from the 'Previous Address Postcode Lookup' page" in {
        navigator.nextPage(CompanyPreviousAddressPostCodeLookupId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyPreviousAddressController.onPageLoad(NormalMode)
      }

      "Go to the 'Contact' page from the 'Previous Address' page" in {
        navigator.nextPage(CompanyPreviousAddressId, NormalMode)(mock[UserAnswers]) mustBe
          routes.ContactDetailsController.onPageLoad(NormalMode)
      }

      "Go to the 'Check Your Answers' page from the 'Contact details' page" in {
        navigator.nextPage(ContactDetailsId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
