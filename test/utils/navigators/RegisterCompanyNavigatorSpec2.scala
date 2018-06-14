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
import controllers.register.company.routes
import identifiers.Identifier
import identifiers.register.BusinessTypeId
import identifiers.register.company._
import models._
import models.requests.IdentifiedRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatest.prop.{TableFor4, TableFor6}
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{NavigatorBehaviour2, UserAnswers}

class RegisterCompanyNavigatorSpec2 extends SpecBase with NavigatorBehaviour2 {

  import RegisterCompanyNavigatorSpec2._

  val navigator = new RegisterCompanyNavigator2(FakeDataCacheConnector)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                   "User Answers",        "Next Page (Normal Mode)",        "Save(NormalMode)", "Next Page (Check Mode)",             "Save(CheckMode"),
    (BusinessTypeId,                         emptyAnswers,           businessDetailsPage,             true,               None,                                 false),
    (BusinessDetailsId,                      emptyAnswers,           confirmCompanyDetailsPage,       true,               None,                                 false),
    (ConfirmCompanyAddressId,                emptyAnswers,           whatYouWillNeedPage,             true,               None,                                 false),
    (WhatYouWillNeedId,                      emptyAnswers,           companyDetailsPage,              true,               None,                                 false),
    (CompanyDetailsId,                       emptyAnswers,           companyRegistrationNumberPage,   true,               Some(checkYourAnswersPage),           false),
    (CompanyRegistrationNumberId,            emptyAnswers,           companyAddressYearsPage,         true,               Some(checkYourAnswersPage),           false),
    (CompanyAddressYearsId,                  addressYearsOverAYear,  contactDetailsPage,              true,               Some(checkYourAnswersPage),           false),
    (CompanyAddressYearsId,                  addressYearsUnderAYear, paPostCodePage(NormalMode),      true,               Some(paPostCodePage(CheckMode)),      true),
    (CompanyAddressYearsId,                  emptyAnswers,           sessionExpiredPage,              false,              Some(sessionExpiredPage),             false),
    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers,           paAddressListPage(NormalMode),   false,              Some(paAddressListPage(CheckMode)),   false),
    (CompanyAddressListId,                   emptyAnswers,           previousAddressPage(NormalMode), true,               Some(previousAddressPage(CheckMode)), true),
    (CompanyPreviousAddressId,               emptyAnswers,           contactDetailsPage,              true,               Some(checkYourAnswersPage),           false),
    (ContactDetailsId,                       emptyAnswers,           checkYourAnswersPage,            true,               Some(checkYourAnswersPage),           false),
    (CompanyReviewId,                        emptyAnswers,           declarationPage,                 true,               None,                                 false)
  )

  //scalastyle:on line.size.limit
  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes())
  }
}

object RegisterCompanyNavigatorSpec2 extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  private lazy val businessDetailsPage = routes.BusinessDetailsController.onPageLoad(NormalMode)
  private lazy val confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()
  private lazy val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  private lazy val companyDetailsPage = routes.CompanyDetailsController.onPageLoad(NormalMode)
  private lazy val companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
  private lazy val companyAddressYearsPage = routes.CompanyAddressYearsController.onPageLoad(NormalMode)
  private lazy val contactDetailsPage = routes.ContactDetailsController.onPageLoad(NormalMode)
  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)
  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)
  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)
  private lazy val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

  private lazy val emptyAnswers = UserAnswers(Json.obj())
  private lazy val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private lazy val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
}
