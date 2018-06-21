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
import identifiers.{Identifier, LastPageId}
import identifiers.register.BusinessTypeId
import identifiers.register.company._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._

  val navigator = new RegisterCompanyNavigator(FakeDataCacheConnector)

  //scalastyle:off line.size.limit
  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                      "User Answers",           "Next Page (Normal Mode)",            "Save (NM)",  "Next Page (Check Mode)",                 "Save (CM)"),
    (BusinessTypeId,                              emptyAnswers,           businessDetailsPage,                  false,        None,                                     false),
    (BusinessDetailsId,                           emptyAnswers,           confirmCompanyDetailsPage,            false,        None,                                     false),
    (ConfirmCompanyAddressId,                     emptyAnswers,           whatYouWillNeedPage,                  false,        None,                                     false),
    (ConfirmCompanyAddressId,                     lastPage,               testLastPage,                         false,        None,                                     false),
    (WhatYouWillNeedId,                           emptyAnswers,           sameContactAddress(NormalMode),       true,         None,                                     false),
    (CompanySameContactAddressId,                 isSameContactAddress,   companyAddressYearsPage(NormalMode),  true,         Some(companyAddressYearsPage(CheckMode)), true),
    (CompanySameContactAddressId,                 notSameContactAddress,  contactAddressPostCode(NormalMode),   true,         Some(contactAddressPostCode(CheckMode)),  true),
    (CompanySameContactAddressId,                 emptyAnswers,           sessionExpiredPage,                   false,        Some(sessionExpiredPage),                 false),
    (CompanyContactAddressPostCodeLookupId,       emptyAnswers,           contactAddressList(NormalMode),       true,         Some(contactAddressList(CheckMode)),      true),
    (CompanyContactAddressListId,                 emptyAnswers,           contatAddress(NormalMode),            true,         Some(contatAddress(CheckMode)),           true),
    (CompanyContactAddressId,                     emptyAnswers,           companyAddressYearsPage(NormalMode),  true,         Some(companyAddressYearsPage(CheckMode)), true),

    (CompanyAddressYearsId,                       addressYearsOverAYear,  contactDetailsPage,                   true,         Some(checkYourAnswersPage),               true),
    (CompanyAddressYearsId,                       addressYearsUnderAYear, paPostCodePage(NormalMode),           true,         Some(paPostCodePage(CheckMode)),          true),
    (CompanyAddressYearsId,                       emptyAnswers,           sessionExpiredPage,                   false,        Some(sessionExpiredPage),                 false),

    (CompanyPreviousAddressPostCodeLookupId,      emptyAnswers,           paAddressListPage(NormalMode),        true,         Some(paAddressListPage(CheckMode)),       true),
    (CompanyAddressListId,                        emptyAnswers,           previousAddressPage(NormalMode),      true,         Some(previousAddressPage(CheckMode)),     true),
    (CompanyPreviousAddressId,                    emptyAnswers,           contactDetailsPage,                   true,         Some(checkYourAnswersPage),               true),

    (ContactDetailsId,                            emptyAnswers,           companyDetailsPage,                   true,         Some(checkYourAnswersPage),               true),
    (CompanyDetailsId,                            emptyAnswers,           companyRegistrationNumberPage,        true,         Some(checkYourAnswersPage),               true),
    (CompanyRegistrationNumberId,                 emptyAnswers,           checkYourAnswersPage,                 true,         Some(checkYourAnswersPage),               true),

    (CheckYourAnswersId,                          emptyAnswers,           addCompanyDirectors(NormalMode),      true,         None,                                     false),
    (CompanyReviewId,                             emptyAnswers,           declarationPage,                      true,         None,                                     false)
  )
  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes())
  }
}

object RegisterCompanyNavigatorSpec extends OptionValues {

  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  private def businessDetailsPage = routes.BusinessDetailsController.onPageLoad(NormalMode)
  private def confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()
  private def whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  private def companyDetailsPage = routes.CompanyDetailsController.onPageLoad(NormalMode)
  private def companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
  private def companyAddressYearsPage(mode: Mode) = routes.CompanyAddressYearsController.onPageLoad(mode)
  private def contactDetailsPage = routes.ContactDetailsController.onPageLoad(NormalMode)
  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)
  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)
  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)
  private def declarationPage = controllers.register.routes.DeclarationController.onPageLoad()
  private def sameContactAddress(mode: Mode) = routes.CompanySameContactAddressController.onPageLoad(mode)
  private def contactAddressPostCode(mode: Mode) = routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode)
  private def contactAddressList(mode: Mode) = routes.CompanyContactAddressListController.onPageLoad(mode)
  private def contatAddress(mode: Mode) = routes.CompanyContactAddressController.onPageLoad(mode)
  private def addCompanyDirectors(mode: Mode) = routes.AddCompanyDirectorsController.onPageLoad(mode)
  private def testLastPage = Call("GET", "www.test.com")

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val isSameContactAddress = UserAnswers().companySameContactAddress(true)
  private val notSameContactAddress = UserAnswers().companySameContactAddress(false)
  private lazy val lastPage = UserAnswers(Json.obj())
    .set(LastPageId)(LastPage("GET", "www.test.com")).asOpt.value

}
