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

package utils

import base.SpecBase
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import controllers.register.company.directors.routes
import identifiers._
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.company.directors._
import models._
import models.register.company.directors.{DirectorAddressYears, DirectorUniqueTaxReference}
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator

  val emptyAnswers = new UserAnswers(Json.obj())
  val testIndex = 0

  "Navigator" when {

    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(mock[UserAnswers]) mustBe controllers.routes.IndexController.onPageLoad()
      }

      "go to the DirectorNino page from the DirectorDetails page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorDetailsId(testIndex), NormalMode)(mock[UserAnswers]) mustBe routes.DirectorNinoController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorUtr page from the DirectorNino page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorNinoId(testIndex), NormalMode)(mock[UserAnswers]) mustBe routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorPostcodeLookup page from the DirectorUtr page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorUniqueTaxReferenceId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorAddressResults page from the DirectorPostcodeLookup page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(CompanyDirectorAddressPostCodeLookupId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.CompanyDirectorAddressListController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorAddress page from the DirectorAddressResults page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(CompanyDirectorAddressListId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.DirectorAddressController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorAddressYears page from the DirectorAddress page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorAddressId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.DirectorAddressYearsController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the PreviousAddressPostcodeLookup page from the DirectorAddressYears page when the answer is less than twelve months" in {
        (0 to 10).foreach {
          index =>

            val answers = UserAnswers(Json.obj())
              .set(DirectorAddressYearsId(0))(DirectorAddressYears.LessThanTwelve)
              .asOpt.value

            navigator.nextPage(DirectorAddressYearsId(testIndex), NormalMode)(answers) mustBe
              routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorContactDetails page from the DirectorAddressYears page when the answer is more than twelve months" in {
        (0 to 10).foreach {
          index =>

            val answers = UserAnswers(Json.obj())
              .set(DirectorAddressYearsId(0))(DirectorAddressYears.MoreThanTwelve)
              .asOpt.value

            navigator.nextPage(DirectorAddressYearsId(testIndex), NormalMode)(answers) mustBe
              routes.DirectorContactDetailsController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the PreviousAddressList page from the PreviousPostcodeLookup page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorPreviousAddressPostCodeLookupId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the PreviousAddress page from the PreviousAddressList page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorPreviousAddressListId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.DirectorPreviousAddressController.onPageLoad(NormalMode, testIndex)
        }
      }

      "go to the DirectorContactDetails page from the PreviousAddress page" in {
        (0 to 10).foreach {
          index =>
            navigator.nextPage(DirectorPreviousAddressId(testIndex), NormalMode)(mock[UserAnswers]) mustBe
              routes.DirectorContactDetailsController.onPageLoad(NormalMode, testIndex)
        }
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from an identifier that doesn't exist in the edit route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, CheckMode)(mock[UserAnswers]) mustBe controllers.routes.CheckYourAnswersController.onPageLoad()
      }

      ".nextPage(DirectorDetailsId)" must {
        "return a `Call` to `CheckYourAnswers` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorDetailsId(index), CheckMode)(emptyAnswers)
              result mustEqual controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }

      ".nextPage(DirectorNinoId)" must {
        "return a `Call` to `CheckYourAnswers` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorNinoId(index), CheckMode)(emptyAnswers)
              result mustEqual controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }

      ".nextPage(DirectorUtrId)" must {
        "return a `Call` to `CheckYourAnswers` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorUniqueTaxReferenceId(index), CheckMode)(emptyAnswers)
              result mustEqual controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }

      ".nextPage(DirectorPostCodeLookupId)" must {
        "return a `Call` to `DirectorAddressList` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(CompanyDirectorAddressPostCodeLookupId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, index)
          }
        }
      }

      ".nextPage(DirectorAddressListId)" must {
        "return a `Call` to `DirectorAddress` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(CompanyDirectorAddressListId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.DirectorAddressController.onPageLoad(CheckMode, index)
          }
        }
      }

      ".nextPage(DirectorAddressId)" must {
        "return a `Call` to `DirectorCheckYourAnswers` page" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorAddressId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }

      ".nextPage(AddressYears)" must {

        "return a `Call` to `DirectorCheckYourAnswersPage` page when `DirectorAddressYears` is `more_than_twelve`" in {
          val answers = UserAnswers(Json.obj())
            .set(DirectorAddressYearsId(0))(DirectorAddressYears.MoreThanTwelve)
            .asOpt.value

          val result = navigator.nextPage(DirectorAddressYearsId(0), CheckMode)(answers)
          result mustEqual routes.CheckYourAnswersController.onPageLoad(0)
        }

        "return a `Call` to `DirectorPreviousPostCodeLookup` page when `DirectorAddressYears` is `less_than_twelve`" in {
          val answers = UserAnswers(Json.obj())
            .set(DirectorAddressYearsId(0))(DirectorAddressYears.LessThanTwelve)
            .asOpt.value

          val result = navigator.nextPage(DirectorAddressYearsId(0), CheckMode)(answers)
          result mustEqual routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, 0)
        }

        "return a `Call` to `SessionExpired` page when `AddressYears` is undefined" in {
          val result = navigator.nextPage(DirectorAddressYearsId(0), CheckMode)(emptyAnswers)
          result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
        }
      }

      ".nextPage(DirectorPreviousAddressPostCodeLookup)" must {
        "return a `Call` to `DirectorPreviousAddressList`" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorPreviousAddressPostCodeLookupId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, index)
          }
        }
      }

      ".nextPage(DirectorPreviousAddressList)" must {
        "return a `Call` to `PreviousAddress`" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorPreviousAddressListId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index)
          }
        }
      }

      ".nextPage(DirectorPreviousAddress)" must {
        "return a `Call` to `CheckYourAnswers`" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorPreviousAddressId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }

      ".nextPage(DirectorContactDetails)" must {
        "return a `Call` to `CheckYourAnswers`" in {
          (0 to 10).foreach {
            index =>
              val result = navigator.nextPage(DirectorContactDetailsId(index), CheckMode)(emptyAnswers)
              result mustEqual routes.CheckYourAnswersController.onPageLoad(index)
          }
        }
      }
    }
  }
}
