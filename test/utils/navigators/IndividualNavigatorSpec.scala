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
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.individual._
import models.{AddressYears, CheckMode, NormalMode}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import utils.UserAnswers

class IndividualNavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new IndividualNavigator()

  val emptyAnswers = new UserAnswers(Json.obj())

  "Navigator" when {

    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(mock[UserAnswers]) mustBe controllers.routes.IndexController.onPageLoad()
      }

      "go to the WhatYouWillNeed page from the IndividualDetails page when the answer is Yes" in {
        (0 to 10).foreach {
          x =>
            val answers = new UserAnswers(Json.obj(
              IndividualDetailsCorrectId.toString -> true
            ))
            navigator.nextPage(IndividualDetailsCorrectId, NormalMode)(answers) mustBe
              routes.WhatYouWillNeedController.onPageLoad()
        }
      }

      "go to the YouWillNeedToUpdate page from the IndividualDetails page when the answer is No" in {
        (0 to 10).foreach {
          x =>
            val answers = new UserAnswers(Json.obj(
              IndividualDetailsCorrectId.toString -> false
            ))

            navigator.nextPage(IndividualDetailsCorrectId, NormalMode)(answers) mustBe
              routes.YouWillNeedToUpdateController.onPageLoad()
        }
      }

      "go to the AddressYears page from the WhatYouWillNeed page" in {
        (0 to 10).foreach {
          x =>
            navigator.nextPage(WhatYouWillNeedId, NormalMode)(mock[UserAnswers]) mustBe
              routes.IndividualAddressYearsController.onPageLoad(NormalMode)
        }
      }

      "go to the PreviousAddressPostcodeLookup page from the AddressYears page when the answer is UnderAYear" in {
        (0 to 10).foreach {
          x =>

            val answers = new UserAnswers(Json.obj(
              IndividualAddressYearsId.toString -> AddressYears.UnderAYear.toString
            ))

            navigator.nextPage(IndividualAddressYearsId, NormalMode)(answers) mustBe
              routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
        }
      }

      "go to the ContactDetails page from the AddressYears page when the answer is OverAYear" in {
        (0 to 10).foreach {
          x =>
            val answers = new UserAnswers(Json.obj(
              IndividualAddressYearsId.toString -> AddressYears.OverAYear.toString
            ))

            navigator.nextPage(IndividualAddressYearsId, NormalMode)(answers) mustBe
              routes.IndividualContactDetailsController.onPageLoad(NormalMode)
        }
      }

      "go to the PreviousAddressList page from the PreviousAddressPostcodeLookup page" in {
        (0 to 10).foreach {
          x =>
            navigator.nextPage(IndividualPreviousAddressPostCodeLookupId, NormalMode)(mock[UserAnswers]) mustBe
              routes.IndividualPreviousAddressListController.onPageLoad(NormalMode)
        }
      }

      "go to the PreviousAddress page from the PreviousAddressList page" in {
        (0 to 10).foreach {
          x =>
            navigator.nextPage(IndividualPreviousAddressListId, NormalMode)(mock[UserAnswers]) mustBe
              routes.IndividualPreviousAddressController.onPageLoad(NormalMode)
        }
      }

      "go to the ContactDetails page from the PreviousAddress page" in {
        (0 to 10).foreach {
          x =>
            navigator.nextPage(IndividualPreviousAddressId, NormalMode)(mock[UserAnswers]) mustBe
              routes.IndividualContactDetailsController.onPageLoad(NormalMode)
        }
      }

      "go to the CheckYourAnswers page from the ContactDetails page" in {
        (0 to 10).foreach {
          x =>
            navigator.nextPage(IndividualContactDetailsId, NormalMode)(mock[UserAnswers]) mustBe
              routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from an identifier that doesn't exist in the edit route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, CheckMode)(mock[UserAnswers]) mustBe controllers.routes.CheckYourAnswersController.onPageLoad()
      }

      "return to CheckYourAnswersPage page when AddressYears is OverAYear" in {
        (0 to 10).foreach {
          x =>
            val answers = new UserAnswers(Json.obj(
              IndividualAddressYearsId.toString -> AddressYears.OverAYear.toString
            ))

            val result = navigator.nextPage(IndividualAddressYearsId, CheckMode)(answers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "return to PreviousPostCodeLookup page when AddressYears is LessThanAYear" in {
        (0 to 10).foreach {
          x =>
            val answers = new UserAnswers(Json.obj(
              IndividualAddressYearsId.toString -> AddressYears.UnderAYear.toString
            ))

            val result = navigator.nextPage(IndividualAddressYearsId, CheckMode)(answers)
            result mustEqual routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
        }
      }

      "return a SessionExpired page when AddressYears is undefined" in {
        (0 to 10).foreach {
          x =>
            val result = navigator.nextPage(IndividualAddressYearsId, CheckMode)(emptyAnswers)
            result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
        }
      }

      "go to AddressList page from PostcodeLookup`" in {
        (0 to 10).foreach {
          x =>
            val result = navigator.nextPage(IndividualPreviousAddressPostCodeLookupId, CheckMode)(emptyAnswers)
            result mustEqual routes.IndividualPreviousAddressListController.onPageLoad(CheckMode)
        }
      }

      "go to Address from PreviousAddressList" in {
        (0 to 10).foreach {
          x =>
            val result = navigator.nextPage(IndividualPreviousAddressListId, CheckMode)(emptyAnswers)
            result mustEqual routes.IndividualPreviousAddressController.onPageLoad(CheckMode)
        }
      }

      "return to `CheckYourAnswers from PreviousAddress`" in {
        (0 to 10).foreach {
          x =>
            val result = navigator.nextPage(IndividualPreviousAddressId, CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "return to CheckYourAnswers from ContactDetails" in {
        (0 to 10).foreach {
          x =>
            val result = navigator.nextPage(IndividualContactDetailsId, CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }
}
