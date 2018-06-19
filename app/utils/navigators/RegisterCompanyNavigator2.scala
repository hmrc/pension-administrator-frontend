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
import connectors.DataCacheConnector
import controllers.register.company.routes
import identifiers.LastPageId
import identifiers.register.BusinessTypeId
import identifiers.register.company._
import models._
import play.api.mvc.Call
import utils.{Navigator2, UserAnswers}

@Singleton
class RegisterCompanyNavigator2 @Inject()(val dataCacheConnector: DataCacheConnector) extends Navigator2 {

  private def checkYourAnswers: Call =
    controllers.register.company.routes.CheckYourAnswersController.onPageLoad()

  //noinspection ScalaStyle
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case BusinessTypeId => NavigateTo.dontSave(routes.BusinessDetailsController.onPageLoad(NormalMode))
    case BusinessDetailsId => NavigateTo.dontSave(routes.ConfirmCompanyDetailsController.onPageLoad())
    case ConfirmCompanyAddressId => detailsCorrect(from.userAnswers)
    case WhatYouWillNeedId => NavigateTo.save(routes.CompanyDetailsController.onPageLoad(NormalMode))
    case CompanyDetailsId => NavigateTo.save(routes.CompanyRegistrationNumberController.onPageLoad(NormalMode))
    case CompanyRegistrationNumberId => NavigateTo.save(routes.CompanyAddressYearsController.onPageLoad(NormalMode))
    case CompanyAddressYearsId => companyAddressYearsIdRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId => NavigateTo.dontSave(routes.CompanyAddressListController.onPageLoad(NormalMode))
    case CompanyAddressListId => NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(NormalMode))
    case CompanyPreviousAddressId => NavigateTo.save(routes.ContactDetailsController.onPageLoad(NormalMode))
    case ContactDetailsId => NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CompanyReviewId => NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case CheckYourAnswersId => NavigateTo.save(routes.AddCompanyDirectorsController.onPageLoad(NormalMode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyDetailsId => NavigateTo.dontSave(checkYourAnswers)
    case CompanyRegistrationNumberId => NavigateTo.dontSave(checkYourAnswers)
    case CompanyAddressId => NavigateTo.dontSave(checkYourAnswers)
    case CompanyAddressYearsId => companyAddressYearsCheckIdRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId => NavigateTo.dontSave(routes.CompanyAddressListController.onPageLoad(CheckMode))
    case CompanyAddressListId => NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(CheckMode))
    case CompanyPreviousAddressId => NavigateTo.dontSave(checkYourAnswers)
    case ContactDetailsId => NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  def detailsCorrect(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(LastPageId) match {
      case Some(lastPage) => NavigateTo.dontSave(Call(lastPage.method, lastPage.url))
      case _ => NavigateTo.save(routes.WhatYouWillNeedController.onPageLoad())
    }
  }

  private def companyAddressYearsIdRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => NavigateTo.save(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))
      case Some(AddressYears.OverAYear) => NavigateTo.save(routes.ContactDetailsController.onPageLoad(NormalMode))
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def companyAddressYearsCheckIdRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => NavigateTo.save(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode))
      case Some(AddressYears.OverAYear) => NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad())
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
