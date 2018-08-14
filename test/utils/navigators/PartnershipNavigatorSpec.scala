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
import connectors.FakeDataCacheConnector
import controllers.register.partnership.routes
import identifiers._
import identifiers.register.partnership._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  val navigator = new PartnershipNavigator(FakeDataCacheConnector)

  //scalastyle:off line.size.limit
  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    (PartnershipDetailsId, emptyAnswers, confirmPartnershipDetailsPage, false, None, false),
    (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue, whatYouWillNeedPage, false, None, false),
    (WhatYouWillNeedId, emptyAnswers, sameContactAddressPage, true, None, true),
    (PartnershipSameContactAddressId, sameContactAddressTrue, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipSameContactAddressId, sameContactAddressFalse, contactPostcodePage(NormalMode), true, Some(contactPostcodePage(CheckMode)), true),
    (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), true, Some(contactAddressListPage(CheckMode)), true),
    (PartnershipContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (PartnershipContactAddressId, emptyAnswers, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipAddressYearsId, addressYearsUnder, contactPreviousPostcodePage(NormalMode), true, Some(contactPreviousPostcodePage(CheckMode)), true),
    (PartnershipAddressYearsId, addressYearsOver, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(NormalMode), true, Some(contactPreviousAddressListPage(CheckMode)), true),
    (PartnershipPreviousAddressListId, emptyAnswers, contactPreviousAddressPage(NormalMode), true, Some(contactPreviousAddressPage(CheckMode)), true),
    (PartnershipPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (PartnershipContactDetailsId, emptyAnswers, vatPage, true, Some(checkYourAnswersPage), true),
    (PartnershipVatId, emptyAnswers, payeNumberPage, true, Some(checkYourAnswersPage), true),
    (PartnershipPayeId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, addPartnersPage, true, None, true),
    (PartnershipReviewId, emptyAnswers, declarationPage, true, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
  }
}

object PartnershipNavigatorSpec extends OptionValues {
  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()

  private def confirmPartnershipDetailsPage = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private def whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()

  private def sameContactAddressPage = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()

  private def vatPage = routes.PartnershipVatController.onPageLoad(NormalMode)

  private def payeNumberPage = routes.PartnershipPayeController.onPageLoad(NormalMode)

  private def contactDetailsPage = routes.PartnershipContactDetailsController.onPageLoad(NormalMode)

  private def addPartnersPage = routes.AddPartnerController.onPageLoad()

  private def addressYearsPage(mode: Mode): Call = routes.PartnershipAddressYearsController.onPageLoad(mode)

  private def contactPostcodePage(mode: Mode): Call = routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode): Call = routes.PartnershipContactAddressListController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode): Call = routes.PartnershipContactAddressController.onPageLoad(mode)

  private def contactPreviousPostcodePage(mode: Mode): Call = routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def contactPreviousAddressListPage(mode: Mode): Call = routes.PartnershipPreviousAddressListController.onPageLoad(mode)

  private def contactPreviousAddressPage(mode: Mode): Call = routes.PartnershipPreviousAddressController.onPageLoad(mode)

  private def declarationPage: Call = controllers.register.routes.DeclarationController.onPageLoad()

  val emptyAnswers = UserAnswers(Json.obj())

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(true).asOpt.value
  private val confirmPartnershipDetailsFalse = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(false).asOpt.value
  private val sameContactAddressTrue = UserAnswers(Json.obj())
    .set(PartnershipSameContactAddressId)(true).asOpt.value
  private val sameContactAddressFalse = UserAnswers(Json.obj())
    .set(PartnershipSameContactAddressId)(false).asOpt.value
  private val addressYearsUnder = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val addressYearsOver = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId)(AddressYears.OverAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}