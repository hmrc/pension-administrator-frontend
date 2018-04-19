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

  val navigator = new RegisterCompanyNavigator(frontendAppConfig)

  private val routes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                      "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (BusinessTypeId,                              emptyAnswers,                   businessDetailsPage,                  None),
    (BusinessDetailsId,                           emptyAnswers,                   confirmCompanyDetailsPage,            Some(checkYourAnswersPage)),
    (ConfirmCompanyAddressId,                     emptyAnswers,                   whatYouWillNeedPage,                  None),
    (WhatYouWillNeedId,                           emptyAnswers,                   companyDetailsPage,                   None),
    (CompanyDetailsId,                            emptyAnswers,                   companyRegistrationNumberPage,        Some(checkYourAnswersPage)),
    (CompanyRegistrationNumberId,                 emptyAnswers,                   companyAddressPage,                   Some(checkYourAnswersPage)),
    (CompanyAddressId,                            emptyAnswers,                   companyAddressYearsPage,              Some(checkYourAnswersPage)),
    (CompanyAddressYearsId,                       addressYearsOverAYear,          contactDetailsPage,                   Some(checkYourAnswersPage)),
    (CompanyAddressYearsId,                       addressYearsUnderAYear,         paPostCodePage(NormalMode),           Some(paPostCodePage(CheckMode))),
    (CompanyAddressYearsId,                       emptyAnswers,                   sessionExpiredPage,                   None),
    (CompanyPreviousAddressPostCodeLookupId,      emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (CompanyAddressListId,                        emptyAnswers,                   previousAddressPage(NormalMode),      Some(previousAddressPage(CheckMode))),
    (CompanyPreviousAddressId,                    emptyAnswers,                   contactDetailsPage,                   Some(checkYourAnswersPage)),
    (ContactDetailsId,                            emptyAnswers,                   checkYourAnswersPage,                 None),
    (CompanyReviewId,                             emptyAnswers,                   declarationPage,                      None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes)
  }
}

object RegisterCompanyNavigatorSpec extends OptionValues {

  val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  val businessDetailsPage = routes.BusinessDetailsController.onPageLoad(NormalMode)
  val confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()
  val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  val companyDetailsPage = routes.CompanyDetailsController.onPageLoad(NormalMode)
  val companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
  val companyAddressPage = routes.CompanyAddressController.onPageLoad()
  val companyAddressYearsPage = routes.CompanyAddressYearsController.onPageLoad(NormalMode)
  val contactDetailsPage = routes.ContactDetailsController.onPageLoad(NormalMode)
  def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)
  def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)
  def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)
  val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

  val emptyAnswers = UserAnswers(Json.obj())
  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
}
