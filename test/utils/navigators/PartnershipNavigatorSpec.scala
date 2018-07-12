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
import identifiers.Identifier
import identifiers.register.partnership._
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import models.{AddressYears, CheckMode, Mode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {
  import PartnershipNavigatorSpec._
  val navigator = new PartnershipNavigator(FakeDataCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                       "User Answers",                  "Next Page (Normal Mode)",                  "Save(NormalMode)", "Next Page (Check Mode)",                        "Save(CheckMode"),
    (PartnershipDetailsId,                       emptyAnswers,                    confirmPartnershipDetailsPage,              true,               None,                                            false),
    (ConfirmPartnershipDetailsId,                confirmPartnershipDetailsTrue,   whatYouWillNeedPage,                        false,              None,                                            false),
    (ConfirmPartnershipDetailsId,                confirmPartnershipDetailsFalse,  companyUpdateDetailsPage,                   false,              None,                                            false),
    (WhatYouWillNeedId,                          emptyAnswers,                    sameContactAddressPage,                     true,               Some(checkYourAnswersPage),                      true),

    (PartnershipSameContactAddressId,            sameContactAddressTrue,          addressYearsPage,                           true,               Some(checkYourAnswersPage),                      true),
    (PartnershipSameContactAddressId,            sameContactAddressFalse,         contactPostcodePage(NormalMode),            true,               Some(contactPostcodePage(CheckMode)),            true),
    (PartnershipSameContactAddressId,            emptyAnswers,                    sessionExpiredPage,                        false,               Some(sessionExpiredPage),                        false),

    (PartnershipContactAddressPostCodeLookupId,  emptyAnswers,                    contactAddressListPage(NormalMode),         true,               Some(contactAddressListPage(CheckMode)),         true),
    (PartnershipContactAddressListId,            emptyAnswers,                    contactAddressPage(NormalMode),             true,               Some(contactAddressPage(CheckMode)),             true),
    (PartnershipContactAddressId,                emptyAnswers,                    addressYearsPage,                           true,               Some(checkYourAnswersPage),                      true),

    (PartnershipAddressYearsId,                  addressYearsUnder,               contactPreviousPostcodePage(NormalMode),    true,               Some(contactPreviousPostcodePage(CheckMode)),    true),
    (PartnershipAddressYearsId,                  addressYearsOver,                vatPage,                                    true,               Some(checkYourAnswersPage),                      true),
    (PartnershipAddressYearsId,                  emptyAnswers,                    sessionExpiredPage,                         true,               Some(sessionExpiredPage),                        true),

    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers,                    contactPreviousAddressListPage(NormalMode), true,               Some(contactPreviousAddressListPage(CheckMode)), true),
    (PartnershipPreviousAddressListId,           emptyAnswers,                    contactPreviousAddressPage(NormalMode),     true,               Some(contactPreviousAddressPage(CheckMode)),     true),
    (PartnershipPreviousAddressId,               emptyAnswers,                    contactDetailsPage,                         true,               Some(checkYourAnswersPage),                      true),

    (PartnershipContactDetailsId,                emptyAnswers,                    vatPage,                                    true,               Some(checkYourAnswersPage),                      true),
    (PartnershipVatId,                           emptyAnswers,                    payeNumberPage,                             true,               Some(checkYourAnswersPage),                      true),
    (PartnershipPayeId,                          emptyAnswers,                    checkYourAnswersPage,                       true,               Some(checkYourAnswersPage),                      true),

    (CheckYourAnswersId,                         emptyAnswers,                    addPartnersPage,                            true,               None,                                            true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
  }
}

object PartnershipNavigatorSpec extends OptionValues {
  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val confirmPartnershipDetailsPage = routes.ConfirmPartnershipDetailsController.onPageLoad()
  private lazy val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  private lazy val companyUpdateDetailsPage = controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()
  private lazy val sameContactAddressPage = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)
  private lazy val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  private lazy val addressYearsPage = routes.PartnershipAddressYearsController.onPageLoad(NormalMode)
  private lazy val vatPage = routes.PartnershipVatController.onPageLoad(NormalMode)
  private lazy val payeNumberPage = routes.PartnershipPayeController.onPageLoad(NormalMode)
  private lazy val contactDetailsPage = routes.PartnershipContactDetailsController.onPageLoad(NormalMode)
  private lazy val addPartnersPage = routes.AddPartnerController.onPageLoad()


  def contactPostcodePage(mode:Mode): Call = routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)
  def contactAddressListPage(mode:Mode): Call = routes.PartnershipContactAddressListController.onPageLoad(mode)
  def contactAddressPage(mode:Mode): Call = routes.PartnershipContactAddressController.onPageLoad(mode)
  def contactPreviousPostcodePage(mode:Mode): Call = routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)
  def contactPreviousAddressListPage(mode:Mode): Call = routes.PartnershipPreviousAddressListController.onPageLoad(mode)
  def contactPreviousAddressPage(mode:Mode): Call = routes.PartnershipPreviousAddressController.onPageLoad(mode)



  val emptyAnswers = UserAnswers(Json.obj())
  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(true).asOpt.value
  private val confirmPartnershipDetailsFalse = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(false).asOpt.value
  private val sameContactAddressTrue = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(true).asOpt.value
  private val sameContactAddressFalse = UserAnswers(Json.obj())
    .set(ConfirmPartnershipDetailsId)(false).asOpt.value
  private val addressYearsUnder = UserAnswers (Json.obj())
    .set(PartnershipAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val addressYearsOver = UserAnswers (Json.obj())
    .set(PartnershipAddressYearsId)(AddressYears.OverAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}

