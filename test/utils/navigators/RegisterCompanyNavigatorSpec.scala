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
import identifiers.Identifier
import identifiers.register.BusinessTypeId
import identifiers.register.company._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._

  val navigator = new RegisterCompanyNavigator

  //scalastyle:off line.size.limit
  private def routes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                      "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (BusinessTypeId,                              emptyAnswers,                   businessDetailsPage,                  None),
    (BusinessDetailsId,                           emptyAnswers,                   confirmCompanyDetailsPage,            None),
    (ConfirmCompanyAddressId,                     emptyAnswers,                   whatYouWillNeedPage,                  None),
    (WhatYouWillNeedId,                           emptyAnswers,                   sameContactAddress(NormalMode),       None),
    (CompanySameContactAddressId,                 isSameContactAddress,           companyAddressYearsPage(NormalMode),  Some(companyAddressYearsPage(CheckMode))),
    (CompanySameContactAddressId,                 notSameContactAddress,          contactAddressPostCode(NormalMode),   Some(contactAddressPostCode(CheckMode))),
    (CompanySameContactAddressId,                 emptyAnswers,                   sessionExpiredPage,                   Some(sessionExpiredPage)),
    (CompanyContactAddressPostCodeLookupId,       emptyAnswers,                   contactAddressList(NormalMode),       Some(contactAddressList(CheckMode))),
    (CompanyContactAddressListId,                 emptyAnswers,                   contatAddress(NormalMode),            Some(contatAddress(CheckMode))),
    (CompanyContactAddressId,                     emptyAnswers,                   companyAddressYearsPage(NormalMode),  Some(checkYourAnswersPage)),

    (CompanyAddressYearsId,                       addressYearsOverAYear,          contactDetailsPage,                   Some(checkYourAnswersPage)),
    (CompanyAddressYearsId,                       addressYearsUnderAYear,         paPostCodePage(NormalMode),           Some(paPostCodePage(CheckMode))),
    (CompanyAddressYearsId,                       emptyAnswers,                   sessionExpiredPage,                   Some(sessionExpiredPage)),

    (CompanyPreviousAddressPostCodeLookupId,      emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (CompanyAddressListId,                        emptyAnswers,                   previousAddressPage(NormalMode),      Some(previousAddressPage(CheckMode))),
    (CompanyPreviousAddressId,                    emptyAnswers,                   contactDetailsPage,                   Some(checkYourAnswersPage)),

    (ContactDetailsId,                            emptyAnswers,                   companyDetailsPage,                   Some(checkYourAnswersPage)),
    (CompanyDetailsId,                            emptyAnswers,                   companyRegistrationNumberPage,        Some(checkYourAnswersPage)),
    (CompanyRegistrationNumberId,                 emptyAnswers,                   checkYourAnswersPage,                 Some(checkYourAnswersPage)),

    (CompanyReviewId,                             emptyAnswers,                   declarationPage,                      None)
  )
  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, routes)
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

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val isSameContactAddress = UserAnswers().companySameContactAddress(true)
  private val notSameContactAddress = UserAnswers().companySameContactAddress(false)

}
