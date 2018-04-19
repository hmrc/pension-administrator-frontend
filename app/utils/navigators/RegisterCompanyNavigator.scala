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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.register.company.routes
import identifiers.Identifier
import identifiers.register.company._
import identifiers.register.BusinessTypeId
import identifiers.register.company.directors.DirectorDetailsId
import models._
import models.register.company.directors.DirectorDetails
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterCompanyNavigator @Inject()(appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(answers: UserAnswers): Call =
    controllers.register.company.routes.CheckYourAnswersController.onPageLoad()

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case BusinessTypeId =>
      _ => routes.BusinessDetailsController.onPageLoad(NormalMode)
    case BusinessDetailsId =>
      _ => routes.ConfirmCompanyDetailsController.onPageLoad()
    case ConfirmCompanyAddressId =>
      _ => routes.WhatYouWillNeedController.onPageLoad()
    case WhatYouWillNeedId =>
      _ => routes.CompanyDetailsController.onPageLoad(NormalMode)
    case CompanyDetailsId =>
      _ => routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
    case CompanyRegistrationNumberId =>
      _ => routes.CompanyAddressController.onPageLoad()
    case CompanyAddressId =>
      _ => routes.CompanyAddressYearsController.onPageLoad(NormalMode)
    case CompanyAddressYearsId => companyAddressYearsIdRoutes
    case CompanyPreviousAddressPostCodeLookupId =>
      _ => routes.CompanyAddressListController.onPageLoad(NormalMode)
    case CompanyAddressListId =>
      _ => routes.CompanyPreviousAddressController.onPageLoad(NormalMode)
    case CompanyPreviousAddressId =>
      _ => routes.ContactDetailsController.onPageLoad(NormalMode)
    case ContactDetailsId =>
      _ => routes.CheckYourAnswersController.onPageLoad()
    case CompanyReviewId =>
      _ => controllers.register.routes.DeclarationController.onPageLoad()
  }

  override protected val editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case BusinessDetailsId => checkYourAnswers
    case CompanyDetailsId => checkYourAnswers
    case CompanyRegistrationNumberId => checkYourAnswers
    case CompanyAddressId => checkYourAnswers
    case CompanyAddressYearsId => companyAddressYearsCheckIdRoutes
    case CompanyPreviousAddressPostCodeLookupId =>
      _ => routes.CompanyAddressListController.onPageLoad(CheckMode)
    case CompanyAddressListId =>
      _ => routes.CompanyPreviousAddressController.onPageLoad(CheckMode)
    case CompanyPreviousAddressId => checkYourAnswers
    case ContactDetailsId =>
      _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def companyAddressYearsIdRoutes(answers: UserAnswers): Call = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case Some(AddressYears.OverAYear) => routes.ContactDetailsController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def companyAddressYearsCheckIdRoutes(answers: UserAnswers): Call = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case Some(AddressYears.OverAYear) => routes.CheckYourAnswersController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}