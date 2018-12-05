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
import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import controllers.register.partnership.routes
import identifiers._
import identifiers.register.partnership._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.FakeCountryOptions
import utils.NavigatorBehaviour
import utils.UserAnswers
import utils.countryOptions.CountryOptions

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigatorUK = new PartnershipNavigator(FakeUserAnswersCacheConnector, countryOptions, frontendAppConfig)
  val navigatorNonUK = new PartnershipNavigator(FakeUserAnswersCacheConnector, countryOptions, frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),

    (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue, whatYouWillNeedPage, false, None, false),
    (WhatYouWillNeedId, emptyAnswers, sameContactAddressPage, true, None, true),
    (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode), true, Some(contactPostcodePage(CheckMode)), true),
    (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), true, Some(contactAddressListPage(CheckMode)), true),
    (PartnershipContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (PartnershipContactAddressId, emptyAnswers, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipAddressYearsId, addressYearsUnderAYearUk, contactPreviousPostcodePage(NormalMode), true, Some(contactPreviousPostcodePage(CheckMode)), true),
    (PartnershipAddressYearsId, addressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(NormalMode), true, Some(contactPreviousAddressListPage(CheckMode)), true),
    (PartnershipPreviousAddressListId, emptyAnswers, contactPreviousAddressPage(NormalMode), true, Some(contactPreviousAddressPage(CheckMode)), true),
    (PartnershipPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (PartnershipVatId, emptyAnswers, payeNumberPage, true, Some(checkYourAnswersPage), true),
    (PartnershipPayeId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, addPartnersPage, true, None, true),
    (PartnershipReviewId, emptyAnswers, declarationPage, true, None, false)
  )
  //scalastyle:off line.size.limit
  private def nonUKRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),

    (PartnershipDetailsId, uk, confirmPartnershipDetailsPage, false, None, false),
    (PartnershipDetailsId, nonUk, nonUkAddress, false, None, false),

    (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode), true, Some(contactPostcodePage(CheckMode)), true),
    (PartnershipSameContactAddressId, notSameContactAddressNonUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (PartnershipAddressYearsId, addressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (PartnershipAddressYearsId, addressYearsUnderAYearUk, contactPreviousPostcodePage(NormalMode), true, Some(contactPreviousPostcodePage(CheckMode)), true),
    (PartnershipAddressYearsId, addressYearsUnderAYearNonUk, contactPreviousAddressPage(NormalMode), true, Some(contactPreviousAddressPage(CheckMode)), true),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (PartnershipContactDetailsId, uk, vatPage, true, Some(checkYourAnswersPage), true),
    (PartnershipContactDetailsId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (PartnershipContactDetailsId, emptyAnswers, sessionExpiredPage, false, Some(checkYourAnswersPage), true),

    (PartnershipRegisteredAddressId, nonUkEuAddress, whatYouWillNeedPage, false, None, false),
    (PartnershipRegisteredAddressId, uKAddress, reconsiderAreYouInUk, false, None, false),
    (PartnershipRegisteredAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false)
  )

  navigatorUK.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigatorUK)
    behave like navigatorWithRoutes(navigatorUK, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }

  s"NonUK ${navigatorNonUK.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigatorNonUK, FakeUserAnswersCacheConnector, nonUKRoutes(), dataDescriber)
  }
}

object PartnershipNavigatorSpec extends OptionValues {

  private def sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private def confirmPartnershipDetailsPage: Call = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private def whatYouWillNeedPage: Call = routes.WhatYouWillNeedController.onPageLoad()

  private def sameContactAddressPage: Call = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private def checkYourAnswersPage: Call = routes.CheckYourAnswersController.onPageLoad()

  private def vatPage: Call = routes.PartnershipVatController.onPageLoad(NormalMode)

  private def payeNumberPage: Call = routes.PartnershipPayeController.onPageLoad(NormalMode)

  private def contactDetailsPage: Call = routes.PartnershipContactDetailsController.onPageLoad(NormalMode)

  private def addPartnersPage: Call = routes.AddPartnerController.onPageLoad()

  private def addressYearsPage(mode: Mode): Call = routes.PartnershipAddressYearsController.onPageLoad(mode)

  private def contactPostcodePage(mode: Mode): Call = routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode): Call = routes.PartnershipContactAddressListController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode): Call = routes.PartnershipContactAddressController.onPageLoad(mode)

  private def contactPreviousPostcodePage(mode: Mode): Call = routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def contactPreviousAddressListPage(mode: Mode): Call = routes.PartnershipPreviousAddressListController.onPageLoad(mode)

  private def contactPreviousAddressPage(mode: Mode): Call = routes.PartnershipPreviousAddressController.onPageLoad(mode)

  private def declarationPage: Call = controllers.register.routes.DeclarationController.onPageLoad()

  private def nonUkAddress: Call = routes.PartnershipRegisteredAddressController.onPageLoad()

  private def reconsiderAreYouInUk: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)

  private def outsideEuEea: Call = routes.OutsideEuEeaController.onPageLoad()

  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)

  private val nonUkEuAddress = UserAnswers().nonUkPartnershipAddress(address("AT"))
  private val uKAddress = UserAnswers().nonUkPartnershipAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkPartnershipAddress(address("AF"))

  private val notSameContactAddressUk = UserAnswers().partnershipSameContactAddress(false).areYouInUk(true)
  private val notSameContactAddressNonUk = UserAnswers().partnershipSameContactAddress(false).areYouInUk(false)
  private val isSameContactAddress = UserAnswers().partnershipSameContactAddress(true)

  private val addressYearsUnderAYearUk = UserAnswers().partnershipAddressYears(AddressYears.UnderAYear).areYouInUk(true)
  private val addressYearsUnderAYearNonUk = UserAnswers().partnershipAddressYears(AddressYears.UnderAYear).areYouInUk(false)
  private val addressYearsOverAYear = UserAnswers().partnershipAddressYears(AddressYears.OverAYear)

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj()).set(ConfirmPartnershipDetailsId)(true).asOpt.value
}
