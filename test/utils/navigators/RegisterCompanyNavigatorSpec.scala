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

import java.time.LocalDate

import base.SpecBase
import controllers.register.company.routes
import identifiers.register.company._
import identifiers.register.company.directors.DirectorDetailsId
import models._
import models.register.company.directors.DirectorDetails
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

      "Go to the 'Previous Address List' page from the 'Previous Address Postcode Lookup' page" in {
        navigator.nextPage(CompanyPreviousAddressPostCodeLookupId, NormalMode)(mock[UserAnswers]) mustBe
          routes.CompanyAddressListController.onPageLoad(NormalMode)
      }

      "Go to the 'Previous Address' page from the 'Previous Address List' page" in {
        navigator.nextPage(CompanyAddressListId, NormalMode)(mock[UserAnswers]) mustBe
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

      "Go to the correct index of directors" must {

        "Go to 'Director Details' page with an index  of (1) if there has been an director already added" in {
          val directorDetails = DirectorDetails("fName", Some("mName"), "lName", LocalDate.of(2012, 12, 31))
          val userAnswers = UserAnswers().set(DirectorDetailsId(0))(directorDetails).asOpt.value

          navigator.nextPage(AddCompanyDirectorsId, NormalMode)(userAnswers) mustBe
            controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, 1)
        }

        "Go to 'Director Details' page with an index  of (0) if there has been no director added" in {

          navigator.nextPage(AddCompanyDirectorsId, NormalMode)(emptyAnswers) mustBe
            controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, 0)
        }
      }

      "Go to the correct page from 'Add Director' Boolean" must {

        "Go to 'Director Details' page when user answers yes to 'Add Director" in {
          val answers = UserAnswers(Json.obj())
            .set(AddCompanyDirectorsId)(true)
            .asOpt.value

          navigator.nextPage(AddCompanyDirectorsId, NormalMode)(answers) mustBe
            controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, 0)
        }

        "Go to 'Company Review' page when user answers no to 'Add Director" in {
          val answers = UserAnswers(Json.obj())
            .set(AddCompanyDirectorsId)(false)
            .asOpt.value

          navigator.nextPage(AddCompanyDirectorsId, NormalMode)(answers) mustBe
            routes.CompanyReviewController.onPageLoad()
        }

        "Go to 'More than 10 Directors' page when there are 10 directors" in {

        }
      }
    }

    "in Check mode" must {

      "Go to the correct page from user's answer" must {

        "if user answers no to business address, go to the 'update address' page" in {

          navigator.nextPage(ConfirmCompanyAddressId, CheckMode)(emptyAnswers) mustBe
            routes.CompanyUpdateDetailsController.onPageLoad()
        }

        "if user answers yes to business address, go to the 'Check your answers" in {

          val answers = UserAnswers(Json.obj())
            .set(ConfirmCompanyAddressId)(TolerantAddress(Some("100"),
              Some("SuttonStreet"),
              Some("Wokingham"),
              Some("Surrey"),
              Some("NE39 1HX"),
              Some("GB")))
            .asOpt.value

          navigator.nextPage(ConfirmCompanyAddressId, CheckMode)(answers) mustBe
            routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "Go to the 'Check your answers' page from 'Company Details' page" in {
        navigator.nextPage(CompanyDetailsId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "Go to the 'Company Address' page from the Company Registration Number page" in {
        navigator.nextPage(CompanyRegistrationNumberId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "Go to the 'Check your answers' page from the 'Company Address' page" in {
        navigator.nextPage(CompanyAddressId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "Go to the correct page from user's answers on Company Address Years" must {

        "Go to the 'Check your answers' page if answers is 'Over a Year'" in {

          val answers = UserAnswers(Json.obj())
            .set(CompanyAddressYearsId)(AddressYears.OverAYear)
            .asOpt.value

          navigator.nextPage(CompanyAddressYearsId, CheckMode)(answers) mustBe
            routes.CheckYourAnswersController.onPageLoad()
        }

        "Go to the 'Previous Address Postcode lookup' page if answers is 'Under a Year'" in {

          val answers = UserAnswers(Json.obj())
            .set(CompanyAddressYearsId)(AddressYears.UnderAYear)
            .asOpt.value

          navigator.nextPage(CompanyAddressYearsId, CheckMode)(answers) mustBe
            routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
        }
      }

      "Go to the 'Previous Address List' page from the 'Previous Address Postcode Lookup' page" in {
        navigator.nextPage(CompanyPreviousAddressPostCodeLookupId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CompanyAddressListController.onPageLoad(CheckMode)
      }

      "Go to the 'Previous Address' page from the 'Previous Address List' page" in {
        navigator.nextPage(CompanyAddressListId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CompanyPreviousAddressController.onPageLoad(CheckMode)
      }

      "Go to the 'Check your answers' page from the 'Previous Address' page" in {
        navigator.nextPage(CompanyPreviousAddressId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "Go to the 'Check Your Answers' page from the 'Contact details' page" in {
        navigator.nextPage(ContactDetailsId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "Go back to 'Company review' page from the 'change company directors' page" in {
        navigator.nextPage(AddCompanyDirectorsId, CheckMode)(mock[UserAnswers]) mustBe
          routes.CompanyReviewController.onPageLoad()
      }
    }
  }
}
